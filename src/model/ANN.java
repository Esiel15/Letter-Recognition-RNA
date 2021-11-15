package model;

public class ANN implements Comparable<ANN>{
    public static final int N_MASK = 0b1111100000000000000;
    public static final int N_MIN = 16;
    public static final int N_MAX = 42;

    public static final int L_MASK = 0b11000000000000;
    public static final int L_MIN = 1;
    public static final int L_MAX = 2;

    public static final int E_MASK = 0b111100000000;
    public static final int E_MIN = 0;
    public static final int E_MAX = 9;

    public static final int LR_MASK = 0b11110000;
    public static final int LR_MIN = 0;
    public static final int LR_MAX = 12;

    public static final int M_MASK = 0b1111;
    public static final int M_MIN = 0;
    public static final int M_MAX = 12;

    public static final int ANN_MASK = 0b1111111111111111111;

    public static final int C1_MASK = 0b1111101111110101010;
    public static final int C2_MASK = 0b0000010000001010101;

    private int ann;
    private double r = -1;

    public ANN() { ann = 0; }
    public ANN(int ann) { setANN(ann); }
    public ANN(int ann, double r) { setANN(ann); this.r = r; }

    public int getANN() { return ann; }
    public void setANN(int ann) {
        ann &= ANN_MASK;
        this.setNeurons(((ann & N_MASK) >> 14) + 26);
        this.setLayers(((ann & L_MASK) >> 12) + 1);
        this.setEpochs(((ann & E_MASK) >> 8));
        this.setLearningRate(((ann & LR_MASK) >> 4));
        this.setMomentum(((ann & M_MASK)));   
    }

    public double getResult() { return r; }
    public void setResult(double r) { this.r = r; }
    
    public int getNeurons(){
        return ((ann & N_MASK) >> 14) + 16;
    }
    
    public void setNeurons(int neuronas){
        if (neuronas >= 16 && neuronas <= 42){
            ann &= ANN_MASK - N_MASK; //Se eliminan las antiguas neuronas
            ann += (neuronas - 16) << 14; //Se colocan las nuevas neuronas
        }
    }

    public int getLayers(){
        return ((ann & L_MASK) >> 12) + 1;
    }
    
    public void setLayers(int layers){
        if (layers >= 1 && layers <= 3){
            ann &= ANN_MASK - L_MASK; //Se eliminan las antiguas capas
            ann += (layers - 1) << 12; ////Se colocan las nuevas capas
        }
    }
    
    public int getEpochs(){
        return ((ann & E_MASK) >> 8) * 200 + 200;
    }
    
    /**El número del numero de épocas sera el 200 + (200 * num)
     * @param num tiene que estar en el rango de 0 - 9*/
    public void setEpochs(int num){
        if (num >= 0 && num <= 9){
            ann &= ANN_MASK - E_MASK; //Se eliminan las antiguas epocas
            ann += num << 8; ////Se colocan las nuevas epocas
        }
    }
    
    public double getLearningRate(){
        return ((ann & LR_MASK) >> 4) * 0.25 + 1.0;
    }
    
    /**El valor del Learning Rate sera 1.0 + (0.025 * num)
     * @param num tiene que estar en el rango de 0 - 3*/
    public void setLearningRate(int num){
        if (num >= 0 && num <= 12){
            ann &= ANN_MASK - LR_MASK; //Se elimina el antiguo learning rate
            ann += num << 4; ////Se coloca el nuevo learning rate
        }
    }

    public double getMomentum(){
        return (ann & M_MASK) * 0.25 + 1.0;
    }
    
    /**El valor del Momentum sera 1.0 + (0.025 * num)
     * @param num tiene que estar en el rango de 0 - 3*/
    public void setMomentum(int num){
        if (num >= 0 && num <= 12){
            ann &= ANN_MASK - M_MASK; //Se elimina el antigua momentum
            ann += num; ////Se coloca el nuevo momentum
        }
    }


    @Override
    public int compareTo(ANN o) {
        return Double.compare(r, o.getResult());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.ann;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        final ANN other = (ANN) obj;
        if (this.ann != other.ann) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString(){
        return "Neurons: " + getNeurons() + " Layers: " + getLayers() + " Epochs: " + getEpochs() 
            + " Learning Rate: " + getLearningRate() + " Momentum: " + getMomentum();
    }
}