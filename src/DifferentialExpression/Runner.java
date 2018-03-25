package DifferentialExpression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Runner {

    public static void main(String[] args) {
        File countfiles = null;
        File labels = null;
        File outdir = null;
        File configSource = null;
        if (args.length > 8 || args.length < 6) {
            System.err.println("Required 6-8 parameters. Found: " + args.length);
            String consoleParameters = "";
            for (int i = 0; i < args.length; i++)
                consoleParameters = consoleParameters.concat(" args[" + i + "]=" + args[i]);
            System.err.println("ConsoleParameters:" + consoleParameters);
        } else {
            // Console parameters are correct!
            for (int i = 0; i < args.length; i = i + 2) {
                switch (args[i]) {
                    case "-countfiles":
                        countfiles = new File(args[i + 1]);
                        continue;
                    case "-labels":
                        labels = new File(args[i + 1]);
                        continue;
                    case "-outdir":
                        outdir = new File(args[i + 1]);
                        continue;
                    case "-config":
                        configSource = new File(args[i + 1]);
                }
            }
        }
        Configuration config = new Configuration(configSource);
        System.out.println("|UserInput|\ncountfiles:\t" + countfiles.getAbsolutePath()
                + "\noutdir:\t" + outdir
                + "\nconfig:\t" + configSource.getAbsolutePath()
                + "\n\t-->rscript.exe:\t" + config.values.get("rscript").getAbsolutePath()
                + "\n\t-->diffscript(de_rseq.R):\t" + config.values.get("diffscript").getAbsolutePath());
        if (labels != null)
            System.out.println("labels:\t" + labels.getAbsolutePath());

        // calculates f_data.txt, p_data.txt and exprs.txt and saves it to outdir
        File exprs = new File(outdir, "/exprs.txt");
        File p_data = new File(outdir, "/p_data.txt");
        File f_data = new File(outdir, "/f_data.txt");
        calculateFdatPdatExprs(countfiles, exprs, p_data, f_data);

        // these files are written by de_rseq.R
        File deSeq = new File(outdir, "/DESeq.out");
        File edgeR = new File(outdir, "/edgeR.out");
        File limma = new File(outdir, "/limma.out");

        String rCom = "\"" + config.values.get("rscript").getAbsolutePath() + "\" \"" + config.values.get("diffscript").getAbsolutePath()
                + "\" \"" + exprs.getAbsolutePath() + "\" \"" + p_data.getAbsolutePath() + "\" \"" + f_data.getAbsolutePath() + "\" ";
//        // DESeq.out
//        R_plotting.runRscript((rCom + "DESeq \"" + deSeq.getAbsolutePath() + "\"").replace("\\", "/"));
//        // edgeR.out
//        R_plotting.runRscript((rCom + "edgeR \"" + edgeR.getAbsolutePath() + "\"").replace("\\", "/"));
//        // limma.out
//        R_plotting.runRscript((rCom + "limma \"" + limma.getAbsolutePath() + "\"").replace("\\", "/"));

        addBenjaminiCorrection(deSeq);
        addBenjaminiCorrection(edgeR);
        addBenjaminiCorrection(limma);

        System.out.println("End of main.");
    }

    private static void calculateFdatPdatExprs(File countfiles, File exprs, File p_data, File f_data) {
        // calculates f_data.txt, p_data.txt and exprs.txt and saves it to outdir
        try {
            LinkedList<PDat> allPDats = new LinkedList<>();
            HashSet<Integer> allConditions = new HashSet<>();
            // --- START retrieve pdat information ---
            BufferedReader countfiles_br = new BufferedReader(new FileReader(countfiles));
            String countfiles_line;
            while ((countfiles_line = countfiles_br.readLine()) != null) {
                // 1. col contains the condition, 2. col = path to the file, 3. col = replicate name
                String[] countSplit = countfiles_line.split("\\t");
                int condition = Integer.parseInt(countSplit[0].substring(1, countSplit[0].length()));
                File geneCountFile = new File(countSplit[1]);
                int replicate = Integer.parseInt(countSplit[2].substring(1, countSplit[2].length()));
                PDat currentPdat = new PDat(replicate, condition, geneCountFile);

                allConditions.add(condition);
                // allPDats.add(replicateName + "\t" + condition);
                allPDats.add(currentPdat);
            }
            countfiles_br.close();
            // --- END retrieve pdat information ---

            // --- START retrieve f_data information ---
            TreeSet<String> allF_data = new TreeSet<>();
            for (PDat pdat : allPDats) {
                for (String key : pdat.convertedHashMap.keySet())
                    allF_data.add(key + "\t" + key);
            }
            System.out.println("F_DATA:" + Arrays.toString(allF_data.toArray()).substring(0, 200));
            System.out.println("F_DATA size: " + allF_data.size());
            // --- END retrieve f_data information ---

            LinkedList<String> allExprs = new LinkedList<>();
            // --- START retrieve exprs information ---
            for (String f_string : allF_data) {
                String currGene = f_string.split("\\t")[0];
                String result = "";
                for (PDat pdat : allPDats) {
                    pdat.convertedHashMap.putIfAbsent(currGene, 0);
                    // System.err.println("No nread found in replicate: " + pdat.replicateName + " condtition: " + pdat.condition + " gene " + currGene);
                    // replace "null"-value with 0 (Integer != int)
                    result = result.concat(pdat.convertedHashMap.get(currGene) + "\t");
                }
                allExprs.add(result);
            }
            // --- END retrieve exprs information ---

            // Create or overwrite new file
            System.out.println("Begin: Write fdat.txt");
            PrintWriter writer = new PrintWriter(f_data, "UTF-8");
            for (String string : allF_data)
                writer.println(string);
            writer.close();
            System.out.println("Begin: Write exprs.txt");
            writer = new PrintWriter(exprs, "UTF-8");
            for (String string : allExprs)
                writer.println(string);
            writer.close();
            System.out.println("Begin: Write pdat.txt");
            writer = new PrintWriter(p_data, "UTF-8");
            for (PDat pdat : allPDats) {
                String temp = "f" + pdat.condition + ".r" + pdat.replicate + "\t" + (pdat.condition - 1);
                writer.println(temp);
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String, Integer> convertFileGeneCoutsToHashMap(File geneCounts) {
        // Key = geneName, Value = nReads
        HashMap<String, Integer> result = new HashMap<>();
        try {
            BufferedReader genecounts_br = new BufferedReader(new FileReader(geneCounts));
            String genecounts_line;
            while ((genecounts_line = genecounts_br.readLine()) != null) {
                // Check if this string is numeric (matches a decimal number with optional -
                if (genecounts_line.split("\\t")[8].matches("-?\\d+(\\.\\d+)?")) {
                    String geneName = genecounts_line.split("\\t")[0];
                    int nreads = Integer.parseInt(genecounts_line.split("\\t")[8]);
                    result.put(geneName, nreads);
                }
            }
            genecounts_br.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.err.println("|convertGeneCoutsFileToHashMap| An error occured.");
        return result;
    }

    private static void addBenjaminiCorrection(File file) {
        ArrayList<Double> allPvalues = new ArrayList<>();
        try {
            BufferedReader countfiles_br = new BufferedReader(new FileReader(file));
            String line = countfiles_br.readLine(); // skip header
            while ((line = countfiles_br.readLine()) != null) {
                String pVal = line.split("\\t")[2];
                if (!pVal.equals("NA")) {
                    //System.out.println("line: " + line + " parsed double: " + Double.parseDouble(pVal));
                    allPvalues.add(Double.parseDouble(pVal));
                }
            }
            double[] correctedPvalues = getBenjaminiHochbergCorrection(allPvalues.toArray(new Double[allPvalues.size()]));
            int counter = 0;
            List<String> newLines = new ArrayList<>();
            for (String currLine : Files.readAllLines(Paths.get(file.getAbsolutePath()), StandardCharsets.UTF_8)) {
                if (currLine.toLowerCase().startsWith("gene.id")) {
                    newLines.add(currLine + "\tADJ.PVAL"); //skip header
                } else if (currLine.split("\t")[2].equals("NA")) {
                    newLines.add(currLine); // skip lines with NA
                } else {
                    newLines.add(currLine + "\t" + correctedPvalues[counter]);
                    counter++;
                }
            }
            Files.write(Paths.get(file.getAbsolutePath()), newLines, StandardCharsets.UTF_8);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static double[] getBenjaminiHochbergCorrection(Double[] pValuesAsArray) {
        //Convert pValues as Array to List
        List<Double> pValues = Arrays.asList(pValuesAsArray);
        List<Double> sortedPValues = new LinkedList<>(pValues);
        Collections.sort(sortedPValues); // ascending
        int n = pValues.size();
        double[] sortedAdjustedPvalues = new double[n];
        double[] adjustedPvalues = new double[n];
        double mFDR = sortedPValues.get(n - 1);
        sortedAdjustedPvalues[n - 1] = mFDR;
        for (int k = n - 1; k > 0; k--) {
            mFDR = Math.min(sortedPValues.get(k - 1) * ((double) n / k), mFDR);
            sortedAdjustedPvalues[k - 1] = mFDR;
        }
        for (int i = 0; i < pValues.size(); i++)
            adjustedPvalues[i] = sortedAdjustedPvalues[sortedPValues.indexOf(pValues.get(i))];
        return adjustedPvalues;
    }

}