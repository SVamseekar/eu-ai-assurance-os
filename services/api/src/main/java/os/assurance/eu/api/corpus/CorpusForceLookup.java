package os.assurance.eu.api.corpus;

@FunctionalInterface
public interface CorpusForceLookup {
  boolean citesFutureDuty(String legalRefs);
}
