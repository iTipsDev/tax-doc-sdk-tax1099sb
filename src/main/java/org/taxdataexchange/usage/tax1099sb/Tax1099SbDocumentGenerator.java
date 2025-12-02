package org.taxdataexchange.usage.tax1099sb;

import irs.fdxtax641.jsonserializers.Tax1099SbSerializer;
import irs.fdxtax641.map2objects.Map2Tax1099Sb;
import org.taxdataexchange.core.pdfbuilders.Tax1099SbPdfBuilder;
import org.taxdataexchange.core.utils.*;
import org.taxdataexchange.core.csv.GenericCsvMapReader;

import org.apache.commons.lang3.time.StopWatch;

import org.openapitools.client.model.*;

import returnagnosticutils.BytesToFile;
import returnagnosticutils.Jsonizer;
import returnagnosticutils.StringToFile;

import java.util.List;
import java.util.Map;

// Read CSV file for one company and generate 1099-MISC PDFs

public class Tax1099SbDocumentGenerator {

    public static final String INPUT_DIRECTORY = "input";

    public static final String OUTPUT_DIRECTORY = "output";

    private void processOneObject(
        int rowNumber,
        String outputDir,
        Tax1099Sb taxObject
    ) {

        // Generate PDFs
        Tax1099SbPdfBuilder pdfBuilder = new Tax1099SbPdfBuilder( );
        StringBuilder stringBuilder = new StringBuilder();
        StopWatch stopWatch = new StopWatch( );
        stopWatch.start( );
        pdfBuilder.buildBasicPdf( stringBuilder, stopWatch, taxObject );
        {
            // Issuer copy
            String fileName = String.format( "%06d.issuer.pdf", rowNumber );
            byte[] pdfBytes = pdfBuilder.getIssuerPdfBytes( );
            BytesToFile.writeToDirFile(
                pdfBytes,
                outputDir,
                fileName
            );
        }

        {
            // Print and mail copy
            byte[] pdfBytes = pdfBuilder.getPrintPdfBytes1( );
            pdfBytes = PdfWatermarker.addWatermarkToPdf( pdfBytes, "Sample" );
            String fileName = String.format( "%06d.print.pdf", rowNumber );
            BytesToFile.writeToDirFile(
                pdfBytes,
                outputDir,
                fileName
            );

            byte[] pngBytes = Pdf2PngConverter.convertBytes( pdfBytes );
            String pngFileName = String.format( "%06d.print.png", rowNumber );
            BytesToFile.writeToDirFile(
                pngBytes,
                outputDir,
                pngFileName
            );
        }

        {
            // Email or download copy
            byte[] pdfBytes = pdfBuilder.getDownloadPdfBytes1( );
            pdfBytes = PdfWatermarker.addWatermarkToPdf( pdfBytes, "Sample" );
            String fileName = String.format( "%06d.download.pdf", rowNumber );
            BytesToFile.writeToDirFile(
                pdfBytes,
                outputDir,
                fileName
            );

            byte[] pngBytes = Pdf2PngConverter.convertBytes( pdfBytes );
            String pngFileName = String.format( "%06d.download.png", rowNumber );
            BytesToFile.writeToDirFile(
                pngBytes,
                outputDir,
                pngFileName
            );

        }

    }

    private void processOneRow(
        String companyName,
        int rowNumber,
        Map<String, String> row
    ) {

        String outputDir = String.format( "%s/%s/%06d", OUTPUT_DIRECTORY, companyName, rowNumber );

        // Save input
        {
            String asJson = Jsonizer.toJson( row );
            String outputFile = String.format( "%06d.map.json", rowNumber );
            StringToFile.writeToDirFile( asJson, outputDir, outputFile );
        }

        // Convert to object and save
        Tax1099Sb obj = Map2Tax1099Sb.generate( row );
        {
            String asJson = Tax1099SbSerializer.serialize( obj );
            String outputFile = String.format( "%06d.obj.json", rowNumber );
            StringToFile.writeToDirFile( asJson, outputDir, outputFile );
        }

        this.processOneObject( rowNumber, outputDir,obj );

    }

    private void processCsvForCompany(
        String companyName
    ) {

        GenericCsvMapReader csvReader = new GenericCsvMapReader( );

        // CSV file
        String csvFileName  = "Tax1099Sb.csv";

        // CSV dir
        String csvDirName = String.format( "%s/%s", INPUT_DIRECTORY, companyName );

        // Read content
        String csvContent = FileUtils.readDirFile(
            csvDirName,
            csvFileName
        );

        // Convert to list of maps
        List<Map<String, String>> rows = csvReader.readStringWithCsvMapReader(
            csvContent
        );

        // Save
        String outputDirName = String.format( "%s/%s", OUTPUT_DIRECTORY, companyName );
        String outputFileName = "Tax1099Sb.rows.json";
        String asJson = Jsonizer.toJson( rows );
        StringToFile.writeToDirFile( asJson, outputDirName, outputFileName );

        // Process each row
        int rowNumber = 0;
        for ( Map<String, String> row : rows ) {

            rowNumber++;
            processOneRow( companyName, rowNumber, row );

        }

    }

    public static void main(String[] args) {

        System.out.println( "Tax1099SbDocumentGenerator Begin" );

        String companyName = "company1";

        Tax1099SbDocumentGenerator generator = new Tax1099SbDocumentGenerator( );

        generator.processCsvForCompany( companyName );

        System.out.println( "Tax1099SbDocumentGenerator Done" );

    }

}
