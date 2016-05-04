package com.waicung.wayfindingx.models;

/**
 * Created by waicung on 04/05/2016.
 */
public class Step {
    private int step_id;
    private Point start_point;
    private Point end_point;
    private String instruction;
    private int duration;
    private int distance;

    public Step(){}

    public Step(int step_id, Point start_point, Point end_point, String instruction, int duration, int distance){
        if(step_id>=0){
            this.step_id = step_id;
        }else{
            this.step_id = 0;
        }
        this.start_point = start_point;
        this.end_point = end_point;
        this.instruction = instruction;
        this.duration = duration;
        this.distance = distance;
    }

    public Step clone(){
        if(start_point!=null &&
                end_point!=null) {
            int step_id = this.step_id;
            Point start_point = this.start_point.clone();
            Point end_point = this.end_point.clone();
            int duration = this.duration;
            int distance = this.distance;
            String instruction = this.instruction;
            return new Step(step_id,start_point,end_point,instruction,duration,distance);
        }
        else {
            String new_instruction = this.instruction;
            Step new_step = new Step();
            new_step.setInstruction(new_instruction);
            return new_step;
        }
    }

    public void setInstruction(String instruction){
        this.instruction = instruction;
    }

}
