package com.toiukha.forum.util;

public class Debug {
	
	//工具類別不實例化
	private Debug() {}
	
	public static void log() {
		System.out.println("here");
	}

	public static void log(int data) {
		System.out.println(data);
	}

	public static void log(double data) {
		System.out.println(data);
	}

	public static void log(long data) {
		System.out.println(data);
	}

	public static void log(char data) {
		System.out.println(data);
	}

	public static void log(String data) {
		System.out.println(data);
	}

	public static void log(Boolean data) {
		System.out.println(data);
	}

	public static void log(Object data) { System.out.println(data);	}

//===================錯誤訊息印出===================

	public static void errorLog(String data) {
		System.err.println("例外： " + data);
	}

	public static void errorLog(String... data) {
//		System.err.println("例外： " + data);
		StringBuilder output = new StringBuilder("Array: ");
		for (int i = 0; i < data.length; i++) {
			if (i == 0) {
				output.append("例外： [").append(data[i]).append(",");
			}else if (i == data.length - 1) {
				output.append(data[i]).append("]");
			}else {
				output.append(data[i]).append(",");
			}
		}
		System.err.println(output);
	}

//===================陣列印出===================
	
	public static void log(int... data) {
		StringBuilder output = new StringBuilder("Array: ");
		for (int i = 0; i < data.length; i++) {
			if (i == 0) {
				output.append("[").append(data[i]).append(",");
			}else if (i == data.length - 1) {
				output.append(data[i]).append("]");
			}else {
				output.append(data[i]).append(",");
			}
		}
		System.out.println(output);
	}

	public static void log(long... data) {
		StringBuilder output = new StringBuilder("Array: ");
		for (int i = 0; i < data.length; i++) {
			if (i == 0) {
				output.append("[").append(data[i]).append(",");
			}else if (i == data.length - 1) {
				output.append(data[i]).append("]");
			}else {
				output.append(data[i]).append(",");
			}
		}
		System.out.println(output);
	}

	public static void log(double... data) {
		StringBuilder output = new StringBuilder("Array: ");
		for (int i = 0; i < data.length; i++) {
			if (i == 0) {
				output.append("[").append(data[i]).append(",");
			}else if (i == data.length - 1) {
				output.append(data[i]).append("]");
			}else {
				output.append(data[i]).append(",");
			}
		}
		System.out.println(output);
	}

	public static void log(char... data) {
		StringBuilder output = new StringBuilder("Array: ");
		for (int i = 0; i < data.length; i++) {
			if (i == 0) {
				output.append("[").append(data[i]).append(",");
			}else if (i == data.length - 1) {
				output.append(data[i]).append("]");
			}else {
				output.append(data[i]).append(",");
			}
		}
		System.out.println(output);
	}

	public static void log(String... data) {
		StringBuilder output = new StringBuilder("Array: ");
		for (int i = 0; i < data.length; i++) {
			if (i == 0) {
				output.append("[").append(data[i]).append(",");
			}else if (i == data.length - 1) {
				output.append(data[i]).append("]");
			}else {
				output.append(data[i]).append(",");
			}
		}
		System.out.println(output);
	}

	public static void log(Object... data) {
		StringBuilder output = new StringBuilder("Array: ");
		for (int i = 0; i < data.length; i++) {
			if (i == 0) {
				output.append("[").append(data[i].toString()).append(",");
			}else if (i == data.length - 1) {
				output.append(data[i].toString()).append("]");
			}else {
				output.append(data[i].toString()).append(",");
			}
		}
		System.out.println(output);
	}

}