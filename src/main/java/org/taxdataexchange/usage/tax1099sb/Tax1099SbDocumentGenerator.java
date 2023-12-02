package org.taxdataexchange.usage.tax1099sb;

import org.taxdataexchange.core.utils.*;
import org.taxdataexchange.docs.tax1099sb.models.*;
import org.taxdataexchange.docs.tax1099sb.utils.*;

public class Tax1099SbDocumentGenerator {

    public static TaxDataList sampleData( ) {

        // -----------------------------------------------------------------
        // Sample data
        // -----------------------------------------------------------------
        TaxData taxData = SampleData.sampleTaxData( "Tax1099Sb" );
        TaxDataList taxDataList = new TaxDataList( );
        taxDataList.addFormsItem( taxData );

        return taxDataList;

    }

    public static PdfOptions getPdfOptions( ) {

        // See https://www.taxdochub.com/pdf-options.html

        PdfOptions pdfOptions = new PdfOptions( );

        pdfOptions.setTriFold( true );
        pdfOptions.setWithAddresses( true );

        pdfOptions.setWithQrCode( true );
        pdfOptions.setQrStyle( PdfOptions.QR_STYLE_PORTRAIT );
        // pdfOptions.setQrStyle( PdfOptions.QR_STYLE_LANDSCAPE );

        pdfOptions.setWithJsonInside( true ) ;

        pdfOptions.setWithInstructions( true );

        pdfOptions.setWithITDStamp( false );
        // pdfOptions.setItdStampType( PdfOptions.ITD_TYPE_LOGO );
        // pdfOptions.setItdStampType( PdfOptions.ITD_TYPE_TEXT );

        pdfOptions.setMaskTin( true );

        return pdfOptions;

    }
    public static byte[] sampleQrAsPng( ) {

        // -----------------------------------------------------------------
        // Generate png
        // -----------------------------------------------------------------

        TaxDataList taxDataList = sampleData( );

        Tax1099SbPdfBuilder pdfBuilder = new Tax1099SbPdfBuilder( );
        byte[] bytes = pdfBuilder.buildQr( taxDataList );

        String filePath = "samples/Tax1099Sb.qr.png";
        FileUtils.bytesToFile( bytes, filePath );
        System.out.println( filePath );

        return bytes;

    }


    public static void samplePdf( ) {

        // -----------------------------------------------------------------
        // Generate PDF
        // -----------------------------------------------------------------

        TaxDataList taxDataList = sampleData( );

        String watermarkText = "Sample"; // Empty string for no watermark

        PdfOptions pdfOptions = getPdfOptions( );

        Tax1099SbPdfBuilder pdfBuilder = new Tax1099SbPdfBuilder( );

        byte[] pdfBytes = pdfBuilder.build( taxDataList, pdfOptions, watermarkText );

        String filePath = "samples/Tax1099Sb.sample.pdf";
        FileUtils.bytesToFile( pdfBytes, filePath );
        System.out.println( filePath );

        String filePathPng = "samples/Tax1099Sb.sample.png";
        byte[] pngBytes = Pdf2PngConverter.convertBytes( pdfBytes );
        FileUtils.bytesToFile( pngBytes, filePathPng );
        System.out.println( filePathPng );


    }

    public static void addTrailerPage() {

        String filePath = "samples/Tax1099Sb.original.pdf";
        byte[] pdfBytes = FileUtils.readByteArray( filePath );

        // QR code including frame. See Tax1099Sb.sample.png
        byte[] pngBytes = sampleQrAsPng( );

        // Place on existing pdf
        byte[] combinedBytes = TrailerPageAdder.insertPngIntoPdf( pngBytes, pdfBytes );
        String filePath2 = "samples/Tax1099Sb.trailer.pdf";
        FileUtils.bytesToFile( combinedBytes, filePath2 );

    }

    public static void addCoverPage() {

        String filePath = "samples/Tax1099Sb.original.pdf";
        byte[] pdfBytes = FileUtils.readByteArray( filePath );

        // QR code including frame. See Tax1099Sb.sample.png
        byte[] pngBytes = sampleQrAsPng( );

        // Place on existing pdf
        byte[] combinedBytes = CoverPageAdder.insertPngIntoPdf( pngBytes, pdfBytes );
        String filePath2 = "samples/Tax1099Sb.cover.pdf";
        FileUtils.bytesToFile( combinedBytes, filePath2 );

    }

    public static void insertQrCode() {

        // Existing document
        String filePath = "samples/Tax1099Sb.original.pdf";
        byte[] pdfBytes = FileUtils.readByteArray( filePath );

        // QR code including frame. See Tax1099Sb.sample.png
        byte[] pngBytes = sampleQrAsPng( );

        // Place on existing pdf
        int pageIndex = 0; // First page is zero
        float y = 72f; // From bottom. Pixels. 72 per inch.
        byte[] combinedBytes = PngInserter.insertPngIntoPdfAt(
            pngBytes,
            pdfBytes,
            pageIndex,
            y
        );
        String filePath2 = "samples/Tax1099Sb.inserted.pdf";
        FileUtils.bytesToFile( combinedBytes, filePath2 );

    }

    public static void main(String[] args) {

        System.out.println( "Tax1099SbDocumentGenerator Begin" );

        // Create a new PDF
        samplePdf( );

        // Insert the QR code on your existing PDF at a specified location
        insertQrCode( );

        // Add cover page with QR code
        addCoverPage( );

        // Add trailer page with QR code
        addTrailerPage( );

        System.out.println( "Tax1099SbDocumentGenerator Done" );

    }

}
