package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

public class GeneticAlgorithm {
    public static final int POBL_SIZE = 30;
    public static final String relativePath = "./pobl/";
    public static final String ext = ".pobl";
    public static final int crossValidation = 5;

    public static final Instances cargarInstancias(String path) throws Exception {
        Instances data = ConverterUtils.DataSource.read(path);
        if (data.classIndex() == -1) data.setClassIndex(0);
        return data;
    }

    public static final ArrayList<ANN> genPobl() {
        ArrayList<ANN> pobl = new ArrayList<>();
        Random random = new Random(System.currentTimeMillis());
        while (pobl.size() < POBL_SIZE) {
            ANN ann = new ANN();
            ann.setNeurons(random.nextInt(ANN.N_MAX + 1 - ANN.N_MIN) + ANN.N_MIN);
            ann.setLayers(random.nextInt(ANN.L_MAX + 1 - ANN.L_MIN) + ANN.L_MIN);
            ann.setEpochs(random.nextInt(ANN.E_MAX + 1 - ANN.E_MIN) + ANN.E_MIN);
            ann.setLearningRate(random.nextInt(ANN.LR_MAX + 1 - ANN.LR_MIN) + ANN.LR_MIN);
            ann.setMomentum(random.nextInt(ANN.M_MAX + 1 - ANN.M_MIN) + ANN.M_MIN);

            if (!pobl.contains(ann)) pobl.add(ann);
        }
        return pobl;
    }

    public static synchronized void saveEvaluation(Evaluation evaluation, ANN ann, int poblN, int gen) throws IOException, Exception {
        File file = new File(relativePath + "results" + poblN + "_gen" + gen + ".txt");
        FileWriter fw = new FileWriter(file, true);
        PrintWriter w = new PrintWriter(new BufferedWriter(fw));

        w.println("||                                  Result                                 ||");
        w.println("-----------------------------------------------------------------------------");
        w.print("|| ANN || ");
        w.println(ann.toString());
        w.println(evaluation.toSummaryString());
        w.println(evaluation.toMatrixString("Confusion Matrix"));
        w.println("-----------------------------------------------------------------------------");
        w.close();
    }

    /**
     * Evalua todos los experimentos que no han sido evaluados o que tiene -1 en su atributo resultado
     * crea un nuevo Hilo por cada experimento a realizar
     * @param poblacion
     * @throws Exception 
     */
    public static void evaluatePobl(ArrayList<ANN> pobl, String filename, int poblN, int gen) throws Exception {
        //Instancias
        Instances instances = cargarInstancias("./assets/letter-recognition.arff");
        
        //Configuracion de los parametros de la RNA
        for (ANN ind : pobl){
            //Si no se ha realizado el experimento lo realiza
            if (ind.getResult() == -1) {
                //Se crea un hilo que ejecutara el experimento
                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        Evaluation evaluation;
                        try {
                            evaluation = new Evaluation(instances);
                            MultilayerPerceptron mlp = new MultilayerPerceptron();
                            
                            StringBuilder hl = new StringBuilder().append(ind.getNeurons());
                            for (int i = 1, c = ind.getLayers(), n = ind.getNeurons() ; i < c ; i++) hl.append(",").append(n);
                            mlp.setHiddenLayers(hl.toString());
                            mlp.setTrainingTime(ind.getEpochs());
                            mlp.setLearningRate(ind.getLearningRate());
                            mlp.setMomentum(ind.getMomentum());
                            
                            //Evaluacion del experimento en hilo
                            evaluation.crossValidateModel(mlp, instances, crossValidation, new Random(1));
                            
                            //Modificar el resultado de la RNA dado en el experimento
                            ind.setResult(evaluation.pctCorrect());
                            
                            savePobl(filename, pobl);
                            saveEvaluation(evaluation, ind, poblN, gen);
                            
                            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                            System.out.println("Termino ANN: " + ind.getANN() + "a las: " + dateFormat.format(new Date())); 
                        } catch (Exception ex) {
                            System.out.println("No se pudo realizar la evaluación");
                            System.err.println(ex.getMessage());
                        } 
                    }
                }).start();
            }
        }
    }

    //Carga la poblacion de un archivo
    public static ArrayList<ANN> loadPobl(String filename) throws FileNotFoundException, IOException, NumberFormatException{
        ArrayList<ANN> pobl = new ArrayList<>();
        
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(relativePath + filename + ext));
            String l;
            while ((l = br.readLine()) != null){
                if (!l.isEmpty()){
                    String[] rna = l.split(",");
                    pobl.add(new ANN(Integer.parseInt(rna[0]), Double.parseDouble(rna[1])));
                }
            }
        }finally{
            if (br != null) br.close();
        }
        
        return pobl;
    }

    //Guarda la poblacion en un archivo
    public static synchronized void savePobl(String filename, ArrayList<ANN> pobl) throws IOException{
        if (pobl != null && pobl.size() > 0){
            //Se ordenan de mayor a menor
            Collections.sort(pobl, Collections.reverseOrder());


            PrintWriter pw = null;
            try{
                new File(relativePath).createNewFile();
                pw = new PrintWriter(new FileWriter(relativePath + filename + ext));
                for (ANN ind : pobl) {
                    pw.println(ind.getANN() + "," + ind.getResult());
                }
            }finally{
                if (pw != null)
                    pw.close();
            }
        }
    }

    //Guarda la poblacion en tres archivo
    public static void savePobl(String filename, ArrayList<ANN> pobl, int gen) throws IOException{
        savePobl(filename.concat("1_gen" + gen), new ArrayList<>(pobl.subList(0, pobl.size()/3)));
        savePobl(filename.concat("2_gen" + gen), new ArrayList<>(pobl.subList(pobl.size()/3, (pobl.size()/3) * 2)));
        savePobl(filename.concat("3_gen" + gen), new ArrayList<>(pobl.subList((pobl.size()/3) * 2, pobl.size())));
    }

    /**Genera descendencia
     * Solo se agregaran a la descendencia si no existen en la poblacion actual
     * ni en la descendencia ya generada
     * @param rnas
     * @return idealmente crea una nueva poblacin de <pobl>
     */
    public static ArrayList<ANN> genOffspring(ArrayList<ANN> anns){
        ArrayList<ANN> desc = new ArrayList<>();
        //Se genera del 25% de la poblacion actual
        for (int i = 0; i < POBL_SIZE/2 ; i++){ 
            ArrayList<ANN> newDesc = genOffspring(anns.get(i), anns.get(POBL_SIZE/2 + i));
            if (!anns.contains(newDesc.get(0)) && !desc.contains(newDesc.get(0)))
                desc.add(newDesc.get(0));
            if (!anns.contains(newDesc.get(1)) && !desc.contains(newDesc.get(1)))
                desc.add(newDesc.get(1));
        }
       return desc; 
    }

    /**Genera dos hijos utilizando las estrategias de cruce X y pulso
     * Solo se agregaran a la descendencia si no existen en la poblacion actual
     */
    public static ArrayList<ANN> genOffspring(ANN ann1, ANN ann2){
        ArrayList<ANN> descPulso = new ArrayList<>();
        descPulso.add(new ANN((ann1.getANN() & ANN.C1_MASK) + (ann2.getANN() & ANN.C2_MASK)));
        descPulso.add(new ANN((ann1.getANN() & ANN.C2_MASK) + (ann2.getANN() & ANN.C1_MASK)));        
        return descPulso;
    }

    /**Genera un desceniente resultado de la mutacion del parametro rna
     * @param rna RNA a la cual se le va a aplicar la mutacion
     * @return retorna la RNA ya mutada
     */
    public static ANN genMutation(ANN ann, int changes){
        ANN mut = new ANN(ann.getANN());
        Random ram = new Random(System.currentTimeMillis());
        for (int i = 1, num ; i < changes ; i++){
            num = ram.nextInt(100) + 1; //1-100
            if (num <= 5){ //Capas 5%
                switch (ram.nextInt(2) + 1){
                    case 1 : mut.setANN(mut.getANN() ^ 0b10000000000000);
                        break;
                    case 2 : mut.setANN(mut.getANN() ^ 0b1000000000000);
                        break;
                }
            }else if (num <= 25){ //Neuronas 20%
                switch (ram.nextInt(5) + 1){
                    case 1 : mut.setANN(mut.getANN() ^ 0b1000000000000000000);
                        break;
                    case 2 : mut.setANN(mut.getANN() ^ 0b100000000000000000);
                        break;
                    case 3 : mut.setANN(mut.getANN() ^ 0b10000000000000000);
                        break;
                    case 4 : mut.setANN(mut.getANN() ^ 0b1000000000000000);
                        break;
                    case 5 : mut.setANN(mut.getANN() ^ 0b100000000000000);
                        break;
                }
            }else if (num <= 40){ //Epocas 15%
                switch (ram.nextInt(4) + 1){
                    case 1 : mut.setANN(mut.getANN() ^ 0b100000000000);
                        break;
                    case 2 : mut.setANN(mut.getANN() ^ 0b10000000000);
                        break;
                    case 3 : mut.setANN(mut.getANN() ^ 0b1000000000);
                        break;
                    case 4 : mut.setANN(mut.getANN() ^ 0b100000000);
                        break;
                }
            }else if (num <= 70){ //Momentum 30%
                switch (ram.nextInt(4) + 1){
                    case 1 : mut.setANN(mut.getANN() ^ 0b1000);
                        break;
                    case 2 : mut.setANN(mut.getANN() ^ 0b100);
                        break;
                    case 3 : mut.setANN(mut.getANN() ^ 0b10);
                        break;
                    case 4 : mut.setANN(mut.getANN() ^ 0b1);
                        break;
                }
            }else{ //Learning Rate 30%
                switch (ram.nextInt(4) + 1){
                    case 1 : mut.setANN(mut.getANN() ^ 0b10000000);
                        break;
                    case 2 : mut.setANN(mut.getANN() ^ 0b1000000);
                        break;
                    case 3 : mut.setANN(mut.getANN() ^ 0b100000);
                        break;
                    case 4 : mut.setANN(mut.getANN() ^ 0b10000);
                        break;
                }
            }
        }
        return mut; 
    }

    /**
     * Este metodo solo debe ser llamado al inicio y una unica vez durante todo el proceso de entrenamiento
     * @param filename nombre del archivo a generar
     */
    public static void firstPobl(String filename) throws Exception{
        ArrayList<ANN> p  = genPobl();
        savePobl(filename, p, 1); //De esta forma se guardan tres archivos diferentes de la 1er generacion
    }

    /**
     * Este metodo debe ser ejecutado cuando se tenga al menos una  
     * RNA en el archivo que no haya sido evaluada en la poblacion. 
     * @param filename archivo donde se encuentra la poblacion
     * @param poblN numero del archivo de la poblacion
     * @param gen generacion de la poblacion
     * @throws Exception 
     */
    public static void evaluatePobl(String filename, int poblN, int gen) throws Exception{
        String f = filename + poblN + "_gen" + gen;
        //Cargar poblacion
        ArrayList<ANN> p = loadPobl(f);
        if (!p.isEmpty()){
            //Evalua y guarda a la poblacion
            evaluatePobl(p, f, poblN, gen);
        }
    }

    /**
     * Este metodo es el encargado de unir tres poblaciones descendientes
     * y unirlas al alchivo que contiene todas las RNAs
     * 
     * Las poblaciones descendientes son agregadas aunque estas ya se encuntren la poblacion
     * 
     * @param filename archivo que contiene TODAS las generaciones de rnas
     * @param gen generacion de descedientes que se debe de unir a la poblacion
     * @throws NumberFormatException
     * @throws IOException 
     */
    public static void joinPobls(String filename, int gen) throws NumberFormatException, IOException{
        ArrayList<ANN> p = new ArrayList<>();
        try{
            p.addAll(loadPobl(filename));
        }catch(FileNotFoundException ex){}
        ArrayList<ANN> p1 = loadPobl(filename.concat("1_gen" + gen));
        ArrayList<ANN> p2 = loadPobl(filename.concat("2_gen" + gen));
        ArrayList<ANN> p3 = loadPobl(filename.concat("3_gen" + gen));
        p.addAll(p1); p.addAll(p2); p.addAll(p3);

        savePobl(filename, p);
    }

    public static void geneticAlgorithm(String filename, int gen) throws NumberFormatException, IOException {
        //Carga TODA la poblacion que ha sido creada a traves de generaciones 
        ArrayList<ANN> anns = loadPobl(filename);
        Random ram = new Random(System.currentTimeMillis());
        
        //Genera descendencia
        ArrayList<ANN> desc = genOffspring(anns);
        
        //Genera mutaciones hasta que haya <pobl> hijos o hasta llegar al limite de intentos (5)
        for (int i = 0 ; (i < 5 && desc.size() < POBL_SIZE) ; i++) {
            for (int j = 0, size = desc.size() ; j < (POBL_SIZE - size) ; j++) {
                ANN ann = genMutation(anns.get(ram.nextInt(5)), 3); //Se toma aleatoriamente 1 rna de las 5 mejores
                if (!anns.contains(ann) && !desc.contains(ann))
                    desc.add(ann);
            }
        }
        //Guarda la descendencia para ser evaluada
        savePobl(filename, desc, gen);
    }
    
}
