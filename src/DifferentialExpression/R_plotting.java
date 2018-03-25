package DifferentialExpression;

import java.io.*;

public class R_plotting {


    public static void main(String[] args) {
//        File deSeq = new File(outdir, "/DESeq.out");
//        File edgeR = new File(outdir, "/edgeR.out");
//        File limma = new File(outdir, "/limma.out");
    }

    static void runRscript(String rCom) {
        try {
            System.out.println("Entered Command: " + rCom);
            Process p = new ProcessBuilder(rCom).start();
            p.waitFor();
            String line;
            // --- stdout ---
            BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = stdout.readLine()) != null)
                System.out.println("StdOut: " + line);
            // --- sterr ---
            BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = stderr.readLine()) != null)
                System.err.println("StdErr: " + line);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//    private static void writeStringToFile(String string, File file) {
//        try {
//            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
//            writer.write(string);
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
