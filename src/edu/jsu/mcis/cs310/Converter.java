package edu.jsu.mcis.cs310;

import com.github.cliftonlabs.json_simple.*;
import com.opencsv.*;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

public class Converter {
    
    /*
        
        Consider the following CSV data, a portion of a database of episodes of
        the classic "Star Trek" television series:
        
        "ProdNum","Title","Season","Episode","Stardate","OriginalAirdate","RemasteredAirdate"
        "6149-02","Where No Man Has Gone Before","1","01","1312.4 - 1313.8","9/22/1966","1/20/2007"
        "6149-03","The Corbomite Maneuver","1","02","1512.2 - 1514.1","11/10/1966","12/9/2006"
        
        (For brevity, only the header row plus the first two episodes are shown
        in this sample.)
    
        The corresponding JSON data would be similar to the following; tabs and
        other whitespace have been added for clarity.  Note the curly braces,
        square brackets, and double-quotes!  These indicate which values should
        be encoded as strings and which values should be encoded as integers, as
        well as the overall structure of the data:
        
        {
            "ProdNums": [
                "6149-02",
                "6149-03"
            ],
            "ColHeadings": [
                "ProdNum",
                "Title",
                "Season",
                "Episode",
                "Stardate",
                "OriginalAirdate",
                "RemasteredAirdate"
            ],
            "Data": [
                [
                    "Where No Man Has Gone Before",
                    1,
                    1,
                    "1312.4 - 1313.8",
                    "9/22/1966",
                    "1/20/2007"
                ],
                [
                    "The Corbomite Maneuver",
                    1,
                    2,
                    "1512.2 - 1514.1",
                    "11/10/1966",
                    "12/9/2006"
                ]
            ]
        }
        
        Your task for this program is to complete the two conversion methods in
        this class, "csvToJson()" and "jsonToCsv()", so that the CSV data shown
        above can be converted to JSON format, and vice-versa.  Both methods
        should return the converted data as strings, but the strings do not need
        to include the newlines and whitespace shown in the examples; again,
        this whitespace has been added only for clarity.
        
        NOTE: YOU SHOULD NOT WRITE ANY CODE WHICH MANUALLY COMPOSES THE OUTPUT
        STRINGS!!!  Leave ALL string conversion to the two data conversion
        libraries we have discussed, OpenCSV and json-simple.  See the "Data
        Exchange" lecture notes for more details, including examples.
        
    */
    
    @SuppressWarnings("unchecked")
    public static String csvToJson(String csvString) {
        
        String result = "{}"; // default return value; replace later!
        
        try {
            // Create a CSV parser
            CSVParser parser = new CSVParserBuilder().withSeparator(',').build();
            CSVReader reader = new CSVReaderBuilder(new StringReader(csvString)).withCSVParser(parser).build();
            
            // Read the CSV data
            List<String[]> csvData = reader.readAll();
            
            // Extract column headings (first row)
            String[] colHeadings = csvData.get(0);
            
            // Initialize JSON structures
            JsonObject json = new JsonObject();
            JsonArray prodNums = new JsonArray();
            JsonArray data = new JsonArray();
            
            // Process each row (skip the header row)
            for (int i = 1; i < csvData.size(); i++) {
                String[] row = csvData.get(i);
                
                // Add ProdNum to the ProdNums array
                prodNums.add(row[0]);
                
                // Create a JSON array for the current row's data
                JsonArray rowData = new JsonArray();
                for (int j = 1; j < row.length; j++) {
                    // Convert Season and Episode to integers
                    if (colHeadings[j].equals("Season") || colHeadings[j].equals("Episode")) {
                        rowData.add(Integer.parseInt(row[j]));
                    } else {
                        rowData.add(row[j]);
                    }
                }
                
                // Add the row data to the Data array
                data.add(rowData);
            }
            
            // Add the JSON structures to the main JSON object
            json.put("ProdNums", prodNums);
            json.put("ColHeadings", colHeadings);
            json.put("Data", data);
            
            // Convert the JSON object to a string
            result = Jsoner.serialize(json);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return result.trim();
    }
    
    @SuppressWarnings("unchecked")
    public static String jsonToCsv(String jsonString) {
        
        String result = ""; // default return value; replace later!
        
        try {
            // Parse the JSON string into a JsonObject
        JsonObject json = Jsoner.deserialize(jsonString, new JsonObject());

        // Extract column headings, ProdNums, and Data
        JsonArray colHeadings = (JsonArray) json.get("ColHeadings");
        JsonArray prodNums = (JsonArray) json.get("ProdNums");
        JsonArray data = (JsonArray) json.get("Data");

        // Create a CSV writer
        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer);

        // Write the header row
        String[] headerRow = new String[colHeadings.size()];
        for (int i = 0; i < colHeadings.size(); i++) {
            headerRow[i] = colHeadings.getString(i);
        }
        csvWriter.writeNext(headerRow);

        // Write each data row
        for (int i = 0; i < data.size(); i++) {
            JsonArray rowData = (JsonArray) data.get(i);
            String[] row = new String[colHeadings.size()];

            // Add ProdNum
            row[0] = prodNums.getString(i);

            // Add the rest of the data
            for (int j = 0; j < rowData.size(); j++) {
                String colName = colHeadings.get(j + 1).toString();

                if (colName.equals("Episode")) {
                    // Ensure Episode always has two digits
                    row[j + 1] = String.format("%02d", Integer.parseInt(rowData.get(j).toString()));
                } else if (colName.equals("Season")) {
                    row[j + 1] = rowData.get(j).toString(); // No leading zero needed
                } else {
                    row[j + 1] = rowData.get(j).toString();
                }
            }

            // Write the row to the CSV
            csvWriter.writeNext(row);
        }

        // Get the CSV string
        result = writer.toString();
    }
    catch (Exception e) {
        e.printStackTrace();
    }

    return result.trim();
    }
}