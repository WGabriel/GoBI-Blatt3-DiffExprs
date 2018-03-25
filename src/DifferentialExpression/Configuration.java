package DifferentialExpression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Configuration {
    // Contains the configuration for R-Scripts
    File configSource;
    HashMap<String, File> values = new HashMap<>();

    // Constructor
    public Configuration(File configSource) {
        super();
        File rscript = null;
        File diffscript = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(configSource));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("R")) {
                    rscript = new File(line.split("\\t")[1]);
                } else if (line.startsWith("diffscript")) {
                    diffscript = new File(line.split("\\t")[1]);
                } else {
                    System.err.println("configFile has unknown format: " + line);
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // C:\Program Files\R\R-3.4.3\bin\x64\Rscript.exe
        this.values.put("rscript", rscript); //
        // C:\Users\Gabriel\Desktop\GoBI\Blatt3\DifferentialExpression\scripts\de_rseq.R
        this.values.put("diffscript", diffscript);

    }

}
