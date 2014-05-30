package com.jpl.iotchallenge.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * IoT Challenge 2014
 * @author José Pereda
 */
public class PropertiesUtils
{
  private static final String PROPERTIES_FILE = "Lhings.properties";
  private static Properties properties;

  public static Properties loadProperties()
  {
    if (properties == null) {
      properties = new Properties();
    }
    try
    {
      File propertiesFile = new File(PROPERTIES_FILE);
      if (propertiesFile.exists()) {
        try (FileInputStream in = new FileInputStream(propertiesFile)) {
          properties.load(in);
        }
      }
    } catch (IOException e) {
        System.out.println("Error al cargar preferencias "+e);
    }
    return properties;
  }

  public static void saveProperties() {
    try {
      File propertiesFile = new File(PROPERTIES_FILE);
      try (FileOutputStream out = new FileOutputStream(propertiesFile)) {
        properties.store(out, "Lhings properties");
      }
    } catch (IOException e) {
      System.out.println("Error al guardar preferencias "+e);
    }
  }
}