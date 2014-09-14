package com.example.recipeapp;

import java.util.Arrays;
import java.util.LinkedList;

import android.graphics.Bitmap;

public class Recipe {
//	private ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
	private LinkedList<String> steps;
	public LinkedList<String> string_ingredients = new LinkedList<String>();
	private int current_step = 0;
	public int num_of_ingredients = 0;
	public String line = "";
//	public Bitmap bm;
	
	public Recipe(LinkedList<String> string_ingredients, LinkedList<String> steps) {
		this.string_ingredients = string_ingredients;
		this.steps = steps;
		this.num_of_ingredients = string_ingredients.size();
		this.current_step = 0;
//		this.bm = bm;
	}
	public Recipe() {
		this.string_ingredients.clear();
//		this.bm = null;
	}
	
	public String[] begin() {
		current_step = 1;
		return new String[] {steps.get(current_step-1)};
	}
	
	public String[] nextStep() {
		if (!recipeFinished()) {
			current_step++;
			return new String[] {steps.get(current_step-1)};
		}
		else {
			return new String[] {"Recipe finished."};
		}
	}
	
	public String[] previousStep() {
		if (current_step > 1) {
			current_step--;
			return new String[] {steps.get(current_step-1)};
		}
		else if (current_step == 0) {
			return new String[] {"Recipe not started."};
		}
		else {
			return new String[] {"Recipe just started."};
		}
	}
	
	
	private void setIngredients(String data) {
		string_ingredients = new LinkedList<String>(Arrays.asList(data.split("\n")));
	}
	public String[] getIngredients() {
		String[] temp = new String[string_ingredients.size()];
		for (int i = 0; i < string_ingredients.size(); i++) {
			temp[i] = string_ingredients.get(i);
		}
		return temp;
	}
	
	public boolean recipeFinished() {
		if (current_step == steps.size())
			return true;
		else 
			return false;
	}
	

	public class Ingredient {
		public String name;
		public String quantity;
		public String preparation;
		public String units;
		
		public Ingredient(String name, String quantity, String units, String preparation) {
			this.name = name;
			this.quantity = quantity;
			this.preparation = preparation;
			this.units = units;
		}
		public Ingredient() {
			this.name = "";
			this.quantity = "0";
			this.preparation = "";
			this.units = "";
		}
		
		public String toString() {
			return name + " " + quantity + " " + units + ", " + preparation;
		}
	}
	
	
}

