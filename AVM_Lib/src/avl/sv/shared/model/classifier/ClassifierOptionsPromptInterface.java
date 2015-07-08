package avl.sv.shared.model.classifier;

public interface ClassifierOptionsPromptInterface {
    abstract public ClassifierWeka getClassifier();
    abstract public void setNumelFeatures(int numel);
}
