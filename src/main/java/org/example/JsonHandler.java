package org.example;



import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

public class JsonHandler {

    // التعديل الجوهري هنا: إضافة الـ Adapters للتعامل مع الوقت والتاريخ
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new com.google.gson.TypeAdapter<LocalDate>() {
                @Override
                public void write(com.google.gson.stream.JsonWriter out, LocalDate value) throws IOException {
                    out.value(value != null ? value.format(DateTimeFormatter.ISO_LOCAL_DATE) : null);
                }
                @Override
                public LocalDate read(com.google.gson.stream.JsonReader in) throws IOException {
                    return LocalDate.parse(in.nextString());
                }
            })
            .registerTypeAdapter(LocalTime.class, new com.google.gson.TypeAdapter<LocalTime>() {
                @Override
                public void write(com.google.gson.stream.JsonWriter out, LocalTime value) throws IOException {
                    out.value(value != null ? value.format(DateTimeFormatter.ISO_LOCAL_TIME) : null);
                }
                @Override
                public LocalTime read(com.google.gson.stream.JsonReader in) throws IOException {
                    return LocalTime.parse(in.nextString());
                }
            })
            .setPrettyPrinting() // لجعل الملف مرتباً وسهل القراءة
            .create();

    // حفظ القائمة في ملف JSON
    public static <T> void saveList(List<T> list, String fileName) {
        try (Writer writer = new FileWriter(fileName)) {
            gson.toJson(list, writer);
            JOptionPane.showMessageDialog(null, 
                    "System: Data saved successfully to " + fileName, 
                    "Data Sync", 
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // تحميل القائمة من ملف JSON
    public static <T> List<T> loadList(String fileName, Class<T> clazz) {
        File file = new File(fileName);
        if (!file.exists()) return new ArrayList<>();

        try (Reader reader = new FileReader(fileName)) {
            Type listType = TypeToken.getParameterized(ArrayList.class, clazz).getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}