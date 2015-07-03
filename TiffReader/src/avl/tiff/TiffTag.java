/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avl.tiff;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author benbryan
 */
public enum TiffTag {

    NewSubfileType(254),    /*	A general indication of the kind of data contained in this subfile.	*/
    SubfileType(255),       /*	A general indication of the kind of data contained in this subfile.	*/
    ImageWidth(256),        /*	The number of columns in the image, i.e., the number of pixels per row.	*/
    ImageLength(257),       /*	The number of rows of pixels in the image.	*/
    BitsPerSample(258),     /*	Number of bits per component.	*/
    Compression(259),       /*	Compression scheme used on the image data.	*/
    PhotometricInterpretation(262),/*	The color space of the image data.	*/
    Threshholding(263),     /*	For black and white TIFF files that represent shades of gray, the technique used to convert from gray to black and white pixels.	*/
    CellWidth(264),         /*	The width of the dithering or halftoning matrix used to create a dithered or halftoned bilevel file.	*/
    CellLength(265),        /*	The length of the dithering or halftoning matrix used to create a dithered or halftoned bilevel file.	*/
    FillOrder(266),         /*	The logical order of bits within a byte.	*/
    ImageDescription(270),  /*	A string that describes the subject of the image.	*/
    Make(271),              /*	The scanner manufacturer.	*/
    Model(272),             /*	The scanner model name or number.	*/
    StripOffsets(273),      /*	For each strip, the byte offset of that strip.	*/
    Orientation(274),       /*	The orientation of the image with respect to the rows and columns.	*/
    SamplesPerPixel(277),   /*	The number of components per pixel.	*/
    RowsPerStrip(278),      /*	The number of rows per strip.	*/
    StripByteCounts(279),   /*	For each strip, the number of bytes in the strip after compression.	*/
    MinSampleValue(280),    /*	The minimum component value used.	*/
    MaxSampleValue(281),    /*	The maximum component value used.	*/
    XResolution(282),       /*	The number of pixels per ResolutionUnit in the ImageWidth direction.	*/
    YResolution(283),       /*	The number of pixels per ResolutionUnit in the ImageLength direction.	*/
    PlanarConfiguration(284),/*	How the components of each pixel are stored.	*/
    FreeOffsets(288),       /*	For each string of contiguous unused bytes in a TIFF file, the byte offset of the string.	*/
    FreeByteCounts(289),    /*	For each string of contiguous unused bytes in a TIFF file, the number of bytes in the string.	*/
    GrayResponseUnit(290),  /*	The precision of the information contained in the GrayResponseCurve.	*/
    GrayResponseCurve(291), /*	For grayscale data, the optical density of each possible pixel value.	*/
    ResolutionUnit(296),    /*	The unit of measurement for XResolution and YResolution.	*/
    Software(305),          /*	Name and version number of the software package(s) used to create the image.	*/
    DateTime(306),         /*	Date and time of image creation.	*/
    Artist(315),            /*	Person who created the image.	*/
    HostComputer(316),/*	The computer and/or operating system in use at the time of image creation.	*/
    ColorMap(320),/*	A color map for palette color images.	*/
    ExtraSamples(338),/*	Description of extra components.	*/
    Copyright(33432),/*	Copyright notice.	*/
    DocumentName(269),/*	The name of the document from which this image was scanned.	*/
    PageName(285),/*	The name of the page from which this image was scanned.	*/
    XPosition(286),/*	X position of the image.	*/
    YPosition(287),/*	Y position of the image.	*/
    T4Options(292),/*	Options for Group 3 Fax compression	*/
    T6Options(293),/*	Options for Group 4 Fax compression	*/
    PageNumber(297),/*	The page number of the page from which this image was scanned.	*/
    TransferFunction(301),/*	Describes a transfer function for the image in tabular style.	*/
    Predictor(317),/*	A mathematical operator that is applied to the image data before an encoding scheme is applied.	*/
    WhitePoint(318),/*	The chromaticity of the white point of the image.	*/
    PrimaryChromaticities(319),/*	The chromaticities of the primaries of the image.	*/
    HalftoneHints(321),/*	Conveys to the halftone function the range of gray levels within a colorimetrically-specified image that should retain tonal detail.	*/
    TileWidth(322),/*	The tile width in pixels. This is the number of columns in each tile.	*/
    TileLength(323),/*	The tile length (height) in pixels. This is the number of rows in each tile.	*/
    TileOffsets(324),/*	For each tile, the byte offset of that tile, as compressed and stored on disk.	*/
    TileByteCounts(325),/*	For each tile, the number of (compressed) bytes in that tile.	*/
    BadFaxLines(326),/*	Used in the TIFF-F standard, denotes the number of 'bad' scan lines encountered by the facsimile device.	*/
    CleanFaxData(327),/*	Used in the TIFF-F standard, indicates if 'bad' lines encountered during reception are stored in the data, or if 'bad' lines have been replaced by the receiver.	*/
    ConsecutiveBadFaxLines(328),/*	Used in the TIFF-F standard, denotes the maximum number of consecutive 'bad' scanlines received.	*/
    SubIFDs(330),/*	Offset to child IFDs.	*/
    InkSet(332),/*	The set of inks used in a separated (PhotometricInterpretation=5) image.	*/
    InkNames(333),/*	The name of each ink used in a separated image.	*/
    NumberOfInks(334),/*	The number of inks.	*/
    DotRange(336),/*	The component values that correspond to a 0% dot and 100% dot.	*/
    TargetPrinter(337),/*	A description of the printing environment for which this separation is intended.	*/
    SampleFormat(339),/*	Specifies how to interpret each data sample in a pixel.	*/
    SMinSampleValue(340),/*	Specifies the minimum sample value.	*/
    SMaxSampleValue(341),/*	Specifies the maximum sample value.	*/
    TransferRange(342),/*	Expands the range of the TransferFunction.	*/
    ClipPath(343),/*	Mirrors the essentials of PostScript's path creation functionality.	*/
    XClipPathUnits(344),/*	The number of units that span the width of the image, in terms of integer ClipPath coordinates.	*/
    YClipPathUnits(345),/*	The number of units that span the height of the image, in terms of integer ClipPath coordinates.	*/
    Indexed(346),/*	Aims to broaden the support for indexed images to include support for any color space.	*/
    JPEGTables(347),/*	JPEG quantization and/or Huffman tables.	*/
    OPIProxy(351),/*	OPI-related.	*/
    GlobalParametersIFD(400),/*	Used in the TIFF-FX standard to point to an IFD containing tags that are globally applicable to the complete TIFF file.	*/
    ProfileType(401),/*	Used in the TIFF-FX standard, denotes the type of data stored in this file or IFD.	*/
    FaxProfile(402),/*	Used in the TIFF-FX standard, denotes the 'profile' that applies to this file.	*/
    CodingMethods(403),/*	Used in the TIFF-FX standard, indicates which coding methods are used in the file.	*/
    VersionYear(404),/*	Used in the TIFF-FX standard, denotes the year of the standard specified by the FaxProfile field.	*/
    ModeNumber(405),/*	Used in the TIFF-FX standard, denotes the mode of the standard specified by the FaxProfile field.	*/
    Decode(433),/*	Used in the TIFF-F and TIFF-FX standards, holds information about the ITULAB (PhotometricInterpretation = 10) encoding.	*/
    DefaultImageColor(434),/*	Defined in the Mixed Raster Content part of RFC 2301, is the default color needed in areas where no image is available.	*/
    JPEGProc(512),/*	Old-style JPEG compression field. TechNote2 invalidates this part of the specification.	*/
    JPEGInterchangeFormat(513),/*	Old-style JPEG compression field. TechNote2 invalidates this part of the specification.	*/
    JPEGInterchangeFormatLength(514),/*	Old-style JPEG compression field. TechNote2 invalidates this part of the specification.	*/
    JPEGRestartInterval(515),/*	Old-style JPEG compression field. TechNote2 invalidates this part of the specification.	*/
    JPEGLosslessPredictors(517),/*	Old-style JPEG compression field. TechNote2 invalidates this part of the specification.	*/
    JPEGPointTransforms(518),/*	Old-style JPEG compression field. TechNote2 invalidates this part of the specification.	*/
    JPEGQTables(519),/*	Old-style JPEG compression field. TechNote2 invalidates this part of the specification.	*/
    JPEGDCTables(520),/*	Old-style JPEG compression field. TechNote2 invalidates this part of the specification.	*/
    JPEGACTables(521),/*	Old-style JPEG compression field. TechNote2 invalidates this part of the specification.	*/
    YCbCrCoefficients(529),/*	The transformation from RGB to YCbCr image data.	*/
    YCbCrSubSampling(530),/*	Specifies the subsampling factors used for the chrominance components of a YCbCr image.	*/
    YCbCrPositioning(531),/*	Specifies the positioning of subsampled chrominance components relative to luminance samples.	*/
    ReferenceBlackWhite(532),/*	Specifies a pair of headroom and footroom image data values (codes) for each pixel component.	*/
    StripRowCounts(559),/*	Defined in the Mixed Raster Content part of RFC 2301, used to replace RowsPerStrip for IFDs with variable-sized strips.	*/
    XMP(700),/*	XML packet containing XMP metadata	*/
    Unknown(-1);
    
    private final short id;   // in kilograms

    public short getShort() {
        return (short)id;
    }

    TiffTag(int id) {
        this.id = (short) id;
    }
    
    private static final Map<Short, TiffTag> intToTypeMap = new HashMap<Short, TiffTag>();

    static {
        for (TiffTag type : TiffTag.values()) {
            intToTypeMap.put(type.getShort(), type);
        }
    }

    public static TiffTag fromShort(short i) {
        TiffTag type = intToTypeMap.get(i);
        if (type == null) {
            return TiffTag.Unknown;
        }
        return type;
    }
}
