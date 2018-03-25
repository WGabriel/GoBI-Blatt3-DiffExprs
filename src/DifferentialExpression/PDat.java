package DifferentialExpression;

import java.io.File;
import java.util.HashMap;

public class PDat {
    int replicate;
    int condition;
    File path;
    // create rReads lookup HashMaps for all Replicates
    // Key = geneName, Value = nReads
    public HashMap<String, Integer> convertedHashMap;

    // Constructor
    public PDat(int replicate, int condition, File path) {
        super();
        this.replicate = replicate;
        this.condition = condition;
        this.path = path;
        this.convertedHashMap = Runner.convertFileGeneCoutsToHashMap(path);
    }

}
