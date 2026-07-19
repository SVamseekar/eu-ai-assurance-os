package os.assurance.eu.api.readiness;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Weighted dimension config for certification readiness scoring.
 * Prefix: {@code assurance.certification-readiness.*}
 */
@Component
@ConfigurationProperties(prefix = "assurance.certification-readiness")
public class CertificationReadinessProperties {
  private int readyForReviewThreshold = 90;
  private int notReadyScoreFloor = 50;
  private int evidencePassThreshold = 82;
  private int evalPassThreshold = 85;
  private int evalHardBlockThreshold = 78;

  private int weightRisk = 10;
  private int weightControls = 15;
  private int weightEvidence = 15;
  private int weightEval = 15;
  private int weightContracts = 10;
  private int weightApprovals = 10;
  private int weightOversight = 10;
  private int weightDetermination = 10;
  private int weightAuditChain = 5;

  public int getReadyForReviewThreshold() {
    return readyForReviewThreshold;
  }

  public void setReadyForReviewThreshold(int readyForReviewThreshold) {
    this.readyForReviewThreshold = readyForReviewThreshold;
  }

  public int getNotReadyScoreFloor() {
    return notReadyScoreFloor;
  }

  public void setNotReadyScoreFloor(int notReadyScoreFloor) {
    this.notReadyScoreFloor = notReadyScoreFloor;
  }

  public int getEvidencePassThreshold() {
    return evidencePassThreshold;
  }

  public void setEvidencePassThreshold(int evidencePassThreshold) {
    this.evidencePassThreshold = evidencePassThreshold;
  }

  public int getEvalPassThreshold() {
    return evalPassThreshold;
  }

  public void setEvalPassThreshold(int evalPassThreshold) {
    this.evalPassThreshold = evalPassThreshold;
  }

  public int getEvalHardBlockThreshold() {
    return evalHardBlockThreshold;
  }

  public void setEvalHardBlockThreshold(int evalHardBlockThreshold) {
    this.evalHardBlockThreshold = evalHardBlockThreshold;
  }

  public int getWeightRisk() {
    return weightRisk;
  }

  public void setWeightRisk(int weightRisk) {
    this.weightRisk = weightRisk;
  }

  public int getWeightControls() {
    return weightControls;
  }

  public void setWeightControls(int weightControls) {
    this.weightControls = weightControls;
  }

  public int getWeightEvidence() {
    return weightEvidence;
  }

  public void setWeightEvidence(int weightEvidence) {
    this.weightEvidence = weightEvidence;
  }

  public int getWeightEval() {
    return weightEval;
  }

  public void setWeightEval(int weightEval) {
    this.weightEval = weightEval;
  }

  public int getWeightContracts() {
    return weightContracts;
  }

  public void setWeightContracts(int weightContracts) {
    this.weightContracts = weightContracts;
  }

  public int getWeightApprovals() {
    return weightApprovals;
  }

  public void setWeightApprovals(int weightApprovals) {
    this.weightApprovals = weightApprovals;
  }

  public int getWeightOversight() {
    return weightOversight;
  }

  public void setWeightOversight(int weightOversight) {
    this.weightOversight = weightOversight;
  }

  public int getWeightDetermination() {
    return weightDetermination;
  }

  public void setWeightDetermination(int weightDetermination) {
    this.weightDetermination = weightDetermination;
  }

  public int getWeightAuditChain() {
    return weightAuditChain;
  }

  public void setWeightAuditChain(int weightAuditChain) {
    this.weightAuditChain = weightAuditChain;
  }

  public int totalWeight() {
    return Math.max(1,
        weightRisk + weightControls + weightEvidence + weightEval + weightContracts
            + weightApprovals + weightOversight + weightDetermination + weightAuditChain);
  }
}
