import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeneradorSecuenciasADN {

    public static String generarSecuencia(int longitud, double[] probabilidades, String Nucleotidos) {
        StringBuilder secuencia = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < longitud; i++) {
            double r = random.nextDouble();
            double acumulado = 0;
            for (int j = 0; j < probabilidades.length; j++) {
                acumulado += probabilidades[j];
                if (r <= acumulado) {
                    secuencia.append(Nucleotidos.charAt(j));
                    break;
                }
            }
        }
        return secuencia.toString();
    }

    public static void generarSecuenciasParalelo(int numSecuencias, int longitudMin, int longitudMax, double[] probabilidades, String archivo,String Nucleotidos) throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        int secuenciasPorHilo = numSecuencias / Runtime.getRuntime().availableProcessors();
        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            int inicio = i * secuenciasPorHilo;
            int fin = (i + 1) * secuenciasPorHilo;
            executor.submit(() -> {
                try (FileWriter writer = new FileWriter(archivo, true)) {
                    for (int j = inicio; j < fin; j++) {
                        Random random = new Random();
                        int longitud = random.nextInt(longitudMax - longitudMin + 1) + longitudMin;
                        String secuencia = generarSecuencia(longitud, probabilidades, Nucleotidos);
                        writer.write(secuencia);
                    }
                } catch (IOException e) {
                    System.err.println(e);
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

    public static void main(String[] args) throws IOException {
        int numSecuencias = 1000000;
        int longitudMin = 5;
        int longitudMax = 100;
        String Nucleotidos = "ACGT";
        double[] probabilidades = {0.25, 0.25, 0.25, 0.25}; // Probabilidades iguales para A, C, G, T
        String archivo = "Nucleotidos.txt";

        generarSecuenciasParalelo(numSecuencias, longitudMin, longitudMax, probabilidades, archivo,Nucleotidos);
    }
}