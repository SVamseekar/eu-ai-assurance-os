"use client";

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { useCorpus } from "@/hooks/use-corpus";

export default function CorpusPage() {
  const query = useCorpus();
  const corpus = query.data;

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader>
          <CardTitle>Pinned corpus</CardTitle>
          <CardDescription>
            Each provision carries its own force date. A future date is not a current duty.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-3 text-sm">
          {query.isLoading && <p className="text-muted-foreground">Loading corpus.</p>}
          {query.isError && (
            <p className="text-destructive">Corpus could not be loaded. {String(query.error)}</p>
          )}
          {corpus && (
            <>
              <p>
                <span className="text-muted-foreground">corpus_version </span>
                <span className="font-mono text-xs break-all">{corpus.corpusVersion ?? "none"}</span>
              </p>
              <table className="w-full text-left text-xs">
                <thead>
                  <tr className="border-b border-border text-muted-foreground">
                    <th className="py-2 pr-3 font-medium">Instrument</th>
                    <th className="py-2 pr-3 font-medium">Consolidation</th>
                    <th className="py-2 font-medium">Application from</th>
                  </tr>
                </thead>
                <tbody>
                  {corpus.instruments.map((instrument) => (
                    <tr key={instrument.seedCelex} className="border-b border-border/60">
                      <td className="py-2 pr-3">
                        <div className="font-medium">{instrument.title}</div>
                        <div className="font-mono text-[10px] text-muted-foreground">{instrument.seedCelex}</div>
                      </td>
                      <td className="py-2 pr-3 font-mono">{instrument.consolidationCelex ?? "—"}</td>
                      <td className="py-2">{instrument.applicationFrom ?? "—"}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Provisions</CardTitle>
          <CardDescription>Annex III high-risk duties stay future until 2 Dec 2027.</CardDescription>
        </CardHeader>
        <CardContent>
          <table className="w-full text-left text-xs">
            <thead>
              <tr className="border-b border-border text-muted-foreground">
                <th className="py-2 pr-3 font-medium">Key</th>
                <th className="py-2 pr-3 font-medium">Force</th>
                <th className="py-2 font-medium">Scope</th>
              </tr>
            </thead>
            <tbody>
              {(corpus?.provisions ?? []).map((provision) => (
                <tr key={provision.provisionKey} className="border-b border-border/60 align-top">
                  <td className="py-2 pr-3 font-mono">{provision.provisionKey}</td>
                  <td className="py-2 pr-3">
                    <div>{provision.forceStatus}</div>
                    <div className="text-muted-foreground">{provision.forceFrom}</div>
                    {provision.forceStatus === "FUTURE" && (
                      <div className="mt-1 text-amber-700 dark:text-amber-400">Not currently enforceable</div>
                    )}
                  </td>
                  <td className="py-2">{provision.scopeNote ?? provision.textExcerpt}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Guidance</CardTitle>
          <CardDescription>Interpretation rows. They are not statute instruments.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-3 text-sm">
          {(corpus?.guidance ?? []).map((document) => (
            <div key={document.sourceKey} className="rounded-lg border border-border p-3">
              <p className="font-medium">{document.title}</p>
              <p className="text-xs text-muted-foreground">
                {document.authorityRank} · {document.relation} · {document.interpretsProvisionKey}
              </p>
              <p className="mt-2 text-xs leading-relaxed">{document.body}</p>
            </div>
          ))}
          {corpus?.attribution && (
            <p className="text-[11px] text-muted-foreground">{corpus.attribution}</p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
