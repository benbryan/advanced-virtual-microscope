/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avl.sv.client.solution;

import java.awt.Color;

/**
 *
 * @author benbryan
 */
public class ColormapJet {
    
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
        new float[]{	0f	,	0f	,	0.5625f	},
        new float[]{	0	,	0	,	0.625f	},
        new float[]{	0	,	0	,	0.6875f	},
        new float[]{	0	,	0	,	0.75f	},
        new float[]{	0	,	0	,	0.8125f	},
        new float[]{	0	,	0	,	0.875f	},
        new float[]{	0	,	0	,	0.9375f	},
        new float[]{	0	,	0	,	1	},
        new float[]{	0	,	0.0625f	,	1	},
        new float[]{	0	,	0.125f	,	1	},
        new float[]{	0	,	0.1875f	,	1	},
        new float[]{	0	,	0.25f	,	1	},
        new float[]{	0	,	0.3125f	,	1	},
        new float[]{	0	,	0.375f	,	1	},
        new float[]{	0	,	0.4375f	,	1	},
        new float[]{	0	,	0.5f	,	1	},
        new float[]{	0	,	0.5625f	,	1	},
        new float[]{	0	,	0.625f	,	1	},
        new float[]{	0	,	0.6875f	,	1	},
        new float[]{	0	,	0.75f	,	1	},
        new float[]{	0	,	0.8125f	,	1	},
        new float[]{	0	,	0.875f	,	1	},
        new float[]{	0	,	0.9375f	,	1	},
        new float[]{	0	,	1f	,	1	},
        new float[]{	0.0625f	,	1f	,	0.9375f	},
        new float[]{	0.125f	,	1f	,	0.875f	},
        new float[]{	0.1875f	,	1f	,	0.8125f	},
        new float[]{	0.25f	,	1f	,	0.75f	},
        new float[]{	0.3125f	,	1f	,	0.6875f	},
        new float[]{	0.375f	,	1f	,	0.625f	},
        new float[]{	0.4375f	,	1f	,	0.5625f	},
        new float[]{	0.5f	,	1f	,	0.5f	},
        new float[]{	0.5625f	,	1f	,	0.4375f	},
        new float[]{	0.625f	,	1f	,	0.375f	},
        new float[]{	0.6875f	,	1f	,	0.3125f	},
        new float[]{	0.75f	,	1f	,	0.25f	},
        new float[]{	0.8125f	,	1f	,	0.1875f	},
        new float[]{	0.875f	,	1f	,	0.125f	},
        new float[]{	0.9375f	,	1f	,	0.0625f	},
        new float[]{	1f	,	1f	,	0	},
        new float[]{	1	,	0.9375f	,	0	},
        new float[]{	1	,	0.875f	,	0	},
        new float[]{	1	,	0.8125f	,	0	},
        new float[]{	1	,	0.75f	,	0	},
        new float[]{	1	,	0.6875f	,	0	},
        new float[]{	1	,	0.625f	,	0	},
        new float[]{	1	,	0.5625f	,	0	},
        new float[]{	1	,	0.5f	,	0	},
        new float[]{	1	,	0.4375f	,	0	},
        new float[]{	1	,	0.375f	,	0	},
        new float[]{	1	,	0.3125f	,	0	},
        new float[]{	1	,	0.25f	,	0	},
        new float[]{	1	,	0.1875f	,	0	},
        new float[]{	1	,	0.125f	,	0	},
        new float[]{	1	,	0.0625f	,	0	},
        new float[]{	1	,	0	,	0	},
        new float[]{	0.9375f	,	0	,	0	},
        new float[]{	0.875f	,	0	,	0	},
        new float[]{	0.8125f	,	0	,	0	},
        new float[]{	0.75f	,	0	,	0	},
        new float[]{	0.6875f	,	0	,	0	},
        new float[]{	0.625f	,	0	,	0	},
        new float[]{	0.5625f	,	0	,	0	},
        new float[]{	0.5f	,	0	,	0	}};
}
