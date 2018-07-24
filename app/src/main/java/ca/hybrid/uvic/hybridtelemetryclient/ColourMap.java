package ca.hybrid.uvic.hybridtelemetryclient;

import java.util.ArrayList;
import java.util.List;

public class ColourMap {

    private class ColourPair{

        private int value;
        private String colour;

        ColourPair(int breakpoint, String colour_input){
            value = breakpoint;
            colour  = colour_input;
        }

        int getBreakpoint(){
            return value;
        }

        String getColour(){
            return colour;
        }

        void setBreakpoint(int breakpoint){
            value = breakpoint;
        }

        void setColour(String colour_input){
            colour = colour_input;
        }

    }

    List<ColourPair> map = new ArrayList<>();

    ColourMap(int[] breakpoints, String[] colours){

        for(int i = 0; i < breakpoints.length; i++){
            ColourPair current = new ColourPair(breakpoints[i], colours[i]);
            map.add(current);
        }

    }

    String getColour(int breakpoint){

        ColourPair pastPair = map.get(0);

        for(ColourPair element: map){
            if(element.getBreakpoint() > breakpoint) return pastPair.getColour();
            pastPair = element;
        }

        return map.get(map.size()-1).getColour();

    }

}
