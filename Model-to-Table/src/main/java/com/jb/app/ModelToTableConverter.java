package com.jb.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;



public class ModelToTableConverter {
	static String jarFilePath = "C:/Users/jsheikh/.m2/repository/com/micropro/vin-commonservice/1.0/vin-commonservice-1.0.jar";
	static String packageName = "com.micropro.commonservice.registrationmodel"; // Update to your package name
	static String classNameGlobal = "REGM_Aca_Faculty_Grade_Mst_Model";
	
	static String downloadPath = "D:/junedCredentials/VNIT/Module class/FieldTable.docx";

	public static void main(String[] args) {
		try {
			// Load the JAR file
			File file = new File(jarFilePath);
			URL jarUrl = file.toURI().toURL();
			URLClassLoader classLoader = new URLClassLoader(new URL[] { jarUrl });

			// Open the JAR file and iterate through its entries
			try (JarFile jarFile = new JarFile(file)) {
				jarFile.stream().filter(entry -> entry.getName().endsWith(".class")).forEach(entry -> {
					String className = getClassName(entry);
					if (className.startsWith(packageName)) {
						loadAndPrintClass(classLoader, className);
					}
				});
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getClassName(JarEntry entry) {
		return entry.getName().replace("/", ".").replace(".class", "");
	}

	private static void loadAndPrintClass(URLClassLoader classLoader, String className) {
		try {
			// Load the class
			Class<?> clazz = classLoader.loadClass(className);

			// Check if the class is a model class (based on your own criteria)
			if (className.contains("Model") && clazz.getName().contains(classNameGlobal)) {
				System.out.println("Loaded Class: " + clazz.getName());
			//	printClassDetails(clazz);
				saveDocuments(downloadPath, clazz);
			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}


	public static void saveDocuments(String filePath, Class<?> clazz){
		//Class<?> modelClass = clazz.getClass();
		 try (XWPFDocument document = new XWPFDocument()) {
	            // Create a table in the document
	            XWPFTable table = document.createTable();

	            // Set the table headers
	            XWPFTableRow header = table.getRow(0);
	            header.getCell(0).setText("Response Attributes");
	            header.addNewTableCell().setText("Data Type");
	            header.addNewTableCell().setText("Mandatory \r\n Optional");
	            header.addNewTableCell().setText("Description");

	            // Extract fields from the model class
	            Field[] fields = clazz.getDeclaredFields();
	            for (Field field : fields) {
	            	String fieldName = field.getName();
	    			String fieldType = field.getType().getSimpleName();
	    			String fieldSize = "";
	    			String fieldDescription = "";

	    			// Get field size from @Size annotation if present
	    			if (field.isAnnotationPresent(Size.class)) {
	    				Size size = field.getAnnotation(Size.class);
	    				fieldSize = ""+ size.max();
	    				fieldDescription = size.message();
	    			}

	    			// Get digits information from @Digits annotation if present
	    			if (field.isAnnotationPresent(Digits.class)) {
	    				Digits digits = field.getAnnotation(Digits.class);
	    				fieldSize =  digits.integer() + "," + digits.fraction();
	    				fieldDescription = digits.message();
	    			}

	    			// Get @NotNull annotation as a description
	    			if (field.isAnnotationPresent(NotNull.class)) {
	    				fieldDescription = field.getAnnotation(NotNull.class).message();
	    			}

	                XWPFTableRow row = table.createRow();
	                row.getCell(0).setText(fieldName);
	                row.getCell(1).setText(fieldType + "("+fieldSize+")");
	                row.getCell(2).setText("M");
	                row.getCell(3).setText(fieldDescription);
	                
	             // Print the information for the current field
	    		//	System.out.printf("%-12s | %-10s | %-14s | %s%n", fieldName, fieldSize, fieldType, fieldDescription);
	            }

	            // Save the document
	            try (FileOutputStream out = new FileOutputStream(downloadPath)) {
	                document.write(out);
	            }

	            System.out.println("Model fields saved in " + downloadPath);

	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	}
	
	
}
