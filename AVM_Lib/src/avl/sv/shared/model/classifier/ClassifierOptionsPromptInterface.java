/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avl.sv.shared.model.classifier;

/**
 *
 * @author benbryan
 */
public interface ClassifierOptionsPromptInterface {
    abstract public ClassifierInterface getClassifier();
    abstract public void setNumelFeatures(int numel);
}
