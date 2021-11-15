import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import model.GeneticAlgorithm;

public class App {
    public static void main(String[] args) throws Exception {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        System.out.println("Inicio de ejecucion: " + dateFormat.format(new Date()));
        
        //Algoritmo Genético Básico  (La condición de paro son 10 generaciones)
        //GeneticAlgorithm.firstPobl("pobl");

        int generation = 1;

        //Coria
        //GeneticAlgorithm.evaluatePobl("pobl", 1, generation);

        //Paulo
        //GeneticAlgorithm.evaluatePobl("pobl", 2, generation);

        //Esiel
        GeneticAlgorithm.evaluatePobl("pobl", 3, generation);

        /*
        GeneticAlgorithm.joinPobls("pobl", generation);
        GeneticAlgorithm.geneticAlgorithm("pobl", generation + 1);
        */
    }
}
