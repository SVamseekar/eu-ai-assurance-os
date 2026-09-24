package os.assurance.eu.api.corpus;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class FormexProvisionParserTest {
  private static final String CELEX = "02024R1689-20260727";

  @Test
  void parsesEnglishFormexArticlesParagraphsAndAnnexes() {
    List<ParsedProvision> provisions = new FormexProvisionParser().parse(CELEX, formex());

    assertThat(provisions).extracting(ParsedProvision::provisionKey)
        .containsExactly(
            CELEX + "#3:1:",
            CELEX + "#50:1:a",
            CELEX + "#annexI::",
            CELEX + "#annexIII::");
    assertThat(provision(provisions, CELEX + "#3:1:").text()).contains("Connected products");
    assertThat(provision(provisions, CELEX + "#annexIII::").text()).contains("Article 6(2)");
    assertThat(provision(provisions, CELEX + "#50:1:a").point()).isEqualTo("a");
  }

  private static ParsedProvision provision(List<ParsedProvision> provisions, String key) {
    return provisions.stream().filter(item -> item.provisionKey().equals(key)).findFirst().orElseThrow();
  }

  private static byte[] formex() {
    return """
        <?xml version="1.0" encoding="UTF-8"?>
        <DOC>
          <ACT>
            <ENACTING.TERMS>
              <ARTICLE IDENTIFIER="003">
                <TI.ART>Article 3</TI.ART>
                <PARAG IDENTIFIER="001">
                  <NO.PARAG>1.</NO.PARAG>
                  <ALINEA>Connected products placed on the market.</ALINEA>
                </PARAG>
              </ARTICLE>
              <ARTICLE IDENTIFIER="050">
                <TI.ART>Article 50</TI.ART>
                <PARAG IDENTIFIER="001">
                  <ALINEA>Providers shall ensure transparency.</ALINEA>
                  <POINT IDENTIFIER="a">
                    <NO.P>(a)</NO.P>
                    <ALINEA>Mark synthetic content.</ALINEA>
                  </POINT>
                </PARAG>
              </ARTICLE>
            </ENACTING.TERMS>
          </ACT>
          <ANNEXES>
            <ANNEX IDENTIFIER="ANX_I">
              <ALINEA>Union harmonisation legislation.</ALINEA>
            </ANNEX>
            <ANNEX IDENTIFIER="ANX_III">
              <ALINEA>High-risk AI systems pursuant to Article 6(2).</ALINEA>
            </ANNEX>
          </ANNEXES>
        </DOC>
        """.getBytes(StandardCharsets.UTF_8);
  }
}
