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

        int pobl = Integer.parseInt(args[0]);
        int generation = Integer.parseInt(args[1]);
        GeneticAlgorithm.evaluatePobl("pobl", pobl, generation);

        /*
        GeneticAlgorithm.joinPobls("pobl", generation);
        GeneticAlgorithm.geneticAlgorithm("pobl", generation + 1);
        */
    }
}
