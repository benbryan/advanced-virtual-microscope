package avl.sv.client.solution;

import java.awt.Color;

public class ColormapGray {
    
    static public Color getColor(int idx, int numel){
        int i = Math.round(map.length*((float)idx/(float)(numel-1)));
        if (i > (map.length-1)){i = map.length-1;}
        return new Color(map[i][0], map[i][1], map[i][2]);
    }
    
    static public Color getColor(double min, double max, double value ){
        value = (value-min)/(max-min);
        int i = (int) Math.round((map.length-1)*value);
        if (i > (map.length-1)){i = map.length-1;}
        return new Color(map[i][0], map[i][1], map[i][2]);
    }
    
    static private float[][] map = new float[][]{
        new float[]{	0f,             0f,             0f},
        new float[]{	0.0159f, 	0.0159f, 	0.0159f},
        new float[]{	0.0317f, 	0.0317f, 	0.0317f},
        new float[]{	0.0476f, 	0.0476f, 	0.0476f},
        new float[]{	0.0635f, 	0.0635f, 	0.0635f},
        new float[]{	0.0794f, 	0.0794f, 	0.0794f},
        new float[]{	0.0952f, 	0.0952f, 	0.0952f},
        new float[]{	0.1111f, 	0.1111f, 	0.1111f},
        new float[]{	0.127f, 	0.127f, 	0.127f},
        new float[]{	0.1429f, 	0.1429f, 	0.1429f},
        new float[]{	0.1587f, 	0.1587f, 	0.1587f},
        new float[]{	0.1746f, 	0.1746f, 	0.1746f},
        new float[]{	0.1905f, 	0.1905f, 	0.1905f},
        new float[]{	0.2063f, 	0.2063f, 	0.2063f},
        new float[]{	0.2222f, 	0.2222f, 	0.2222f},
        new float[]{	0.2381f, 	0.2381f, 	0.2381f},
        new float[]{	0.254f, 	0.254f, 	0.254f},
        new float[]{	0.2698f, 	0.2698f, 	0.2698f},
        new float[]{	0.2857f, 	0.2857f, 	0.2857f},
        new float[]{	0.3016f, 	0.3016f, 	0.3016f},
        new float[]{	0.3175f, 	0.3175f, 	0.3175f},
        new float[]{	0.3333f, 	0.3333f, 	0.3333f},
        new float[]{	0.3492f, 	0.3492f, 	0.3492f},
        new float[]{	0.3651f, 	0.3651f, 	0.3651f},
        new float[]{	0.381f, 	0.381f, 	0.381f},
        new float[]{	0.3968f, 	0.3968f, 	0.3968f},
        new float[]{	0.4127f, 	0.4127f, 	0.4127f},
        new float[]{	0.4286f, 	0.4286f, 	0.4286f},
        new float[]{	0.4444f, 	0.4444f, 	0.4444f},
        new float[]{	0.4603f, 	0.4603f, 	0.4603f},
        new float[]{	0.4762f, 	0.4762f, 	0.4762f},
        new float[]{	0.4921f, 	0.4921f, 	0.4921f},
        new float[]{	0.5079f, 	0.5079f, 	0.5079f},
        new float[]{	0.5238f, 	0.5238f, 	0.5238f},
        new float[]{	0.5397f, 	0.5397f, 	0.5397f},
        new float[]{	0.5556f, 	0.5556f, 	0.5556f},
        new float[]{	0.5714f, 	0.5714f, 	0.5714f},
        new float[]{	0.5873f, 	0.5873f, 	0.5873f},
        new float[]{	0.6032f, 	0.6032f, 	0.6032f},
        new float[]{	0.619f, 	0.619f, 	0.619f},
        new float[]{	0.6349f, 	0.6349f, 	0.6349f},
        new float[]{	0.6508f, 	0.6508f, 	0.6508f},
        new float[]{	0.6667f, 	0.6667f, 	0.6667f},
        new float[]{	0.6825f, 	0.6825f, 	0.6825f},
        new float[]{	0.6984f, 	0.6984f, 	0.6984f},
        new float[]{	0.7143f, 	0.7143f, 	0.7143f},
        new float[]{	0.7302f, 	0.7302f, 	0.7302f},
        new float[]{	0.746f, 	0.746f, 	0.746f},
        new float[]{	0.7619f, 	0.7619f, 	0.7619f},
        new float[]{	0.7778f, 	0.7778f, 	0.7778f},
        new float[]{	0.7937f, 	0.7937f, 	0.7937f},
        new float[]{	0.8095f, 	0.8095f, 	0.8095f},
        new float[]{	0.8254f, 	0.8254f, 	0.8254f},
        new float[]{	0.8413f, 	0.8413f, 	0.8413f},
        new float[]{	0.8571f, 	0.8571f, 	0.8571f},
        new float[]{	0.873f, 	0.873f, 	0.873f},
        new float[]{	0.8889f, 	0.8889f, 	0.8889f},
        new float[]{	0.9048f, 	0.9048f, 	0.9048f},
        new float[]{	0.9206f, 	0.9206f, 	0.9206f},
        new float[]{	0.9365f, 	0.9365f, 	0.9365f},
        new float[]{	0.9524f, 	0.9524f, 	0.9524f},
        new float[]{	0.9683f, 	0.9683f, 	0.9683f},
        new float[]{	0.9841f, 	0.9841f, 	0.9841f},
        new float[]{	1f,             1f,             1f}};

    
    
    
    
    
    
    
}
