/*
 * JOCL - Java bindings for OpenCL
 * 
 * Copyright 2009 Marco Hutter - http://www.jocl.org/
 */

package avl.Convolution;

import java.awt.image.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import org.jocl.*;
import static org.jocl.CL.*;


class JOCLConvolveOp {

    private static long round(long groupSize, long globalSize) {
        long r = globalSize % groupSize;
        if(r == 0) {
            return globalSize;
        } else {
            return globalSize + groupSize - r;
        }
    }
    
    private static String readKernel(String fileName) {
        try {
            BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(fileName)));
            StringBuffer sb = new StringBuffer();
            String line = null;
            while (true) {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
 
    private cl_kernel clKernel;
    private cl_context context;
    private cl_command_queue commandQueue;
    
    public JOCLConvolveOp () {
        // The platform, device type and device number
        // that will be used
        final int platformIndex = 0;
        final long deviceType = CL_DEVICE_TYPE_ALL;
        final int deviceIndex = 0;

        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);

        // Obtain the number of platforms
        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        // Obtain a platform ID
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[platformIndex];

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
        
        // Obtain the number of devices for the platform
        int numDevicesArray[] = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];
        
        // Obtain a device ID 
        cl_device_id devices[] = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        cl_device_id device = devices[deviceIndex];

        // Create a context for the selected device
        context = clCreateContext(
            contextProperties, 1, new cl_device_id[]{device}, 
            null, null, null);
        
        // Create a command-queue for the selected device
        commandQueue = clCreateCommandQueue(context, device, 0, null);
        
        loadAndCompileKernel("simpleConvolution.cl");
    }
    
    private void loadAndCompileKernel(String kernelName){
        // Create the OpenCL kernel from the program
        URL kernelUrl = getClass().getResource(kernelName);
        String source = readKernel(kernelUrl.getPath());
        cl_program program = clCreateProgramWithSource(context, 1, 
            new String[]{ source }, null, null);
        String compileOptions = "-cl-mad-enable";
        clBuildProgram(program, 0, null, compileOptions, null, null);
        clKernel = clCreateKernel(program, "simpleConvolution", null);
        clReleaseProgram(program);    
    }


    @Override
    protected void finalize() throws Throwable {
        clReleaseKernel(clKernel);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);          
        super.finalize();
    }
    
    public void filter(BufferedImage src, ArrayList<Convolution_Kernel> cKernels) {
        
        // Validity checks for the given images
        if (src.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            throw new IllegalArgumentException("Source image is not TYPE_BYTE_GRAY");
        }

        int imageSizeX = src.getWidth();
        int imageSizeY = src.getHeight();

        // Create the memory object for the input- and output image
        byte dataSrc[] = ((DataBufferByte)src.getRaster().getDataBuffer()).getData();
        cl_mem inputImageMem = clCreateBuffer(context, 
            CL_MEM_READ_ONLY | CL_MEM_USE_HOST_PTR, 
            dataSrc.length * Sizeof.cl_uchar, 
            Pointer.to(dataSrc), null);

        cl_mem outputImageMem = clCreateBuffer(context, CL_MEM_WRITE_ONLY, 
            imageSizeX * imageSizeY * Sizeof.cl_float, null, null);
        
        for ( Convolution_Kernel cKernel:cKernels){
            Kernel kernel = cKernel.getKernel();
            //load kernel;
            float kernelData[] = kernel.getKernelData(null);
            cl_mem kernelMem = clCreateBuffer(context, CL_MEM_READ_ONLY, 
                kernelData.length * Sizeof.cl_uint, null, null);
            clEnqueueWriteBuffer(commandQueue, kernelMem, 
                true, 0, kernelData.length * Sizeof.cl_uint, 
                Pointer.to(kernelData), 0, null, null);

            // Set work sizes and arguments, and execute the kernel
            int kernelSizeX = kernel.getWidth();
            int kernelSizeY = kernel.getHeight();
            int kernelOriginX = kernel.getXOrigin();
            int kernelOriginY = kernel.getYOrigin();

            long localWorkSize[] = new long[2];
            localWorkSize[0] = 1;
            localWorkSize[1] = 1;

            long globalWorkSize[] = {round(localWorkSize[0], imageSizeX), round(localWorkSize[1], imageSizeY)};

            int imageSize[] = { imageSizeX, imageSizeY };
            int kernelSize[] = { kernelSizeX, kernelSizeY };
            int kernelOrigin[] = { kernelOriginX, kernelOriginY };

            clSetKernelArg(clKernel, 0, Sizeof.cl_mem, Pointer.to(inputImageMem));
            clSetKernelArg(clKernel, 1, Sizeof.cl_mem, Pointer.to(kernelMem));
            clSetKernelArg(clKernel, 2, Sizeof.cl_mem, Pointer.to(outputImageMem));
            clSetKernelArg(clKernel, 3, Sizeof.cl_int2, Pointer.to(imageSize));
            clSetKernelArg(clKernel, 4, Sizeof.cl_int2, Pointer.to(kernelSize));
            clSetKernelArg(clKernel, 5, Sizeof.cl_int2, Pointer.to(kernelOrigin));

            clEnqueueNDRangeKernel(commandQueue, clKernel, 2, null, globalWorkSize, localWorkSize, 0, null, null);

            float data[] = new float[src.getWidth()*src.getHeight()];
            clEnqueueReadBuffer(commandQueue, outputImageMem, 
                CL_TRUE, 0, data.length * Sizeof.cl_float, 
                Pointer.to(data), 0, null, null);

            cKernel.results = data;
            clReleaseMemObject(kernelMem);
        }
        clReleaseMemObject(inputImageMem);
        clReleaseMemObject(outputImageMem);
    }
    
    
}
