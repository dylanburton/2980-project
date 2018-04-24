package ichabod;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.imageio.ImageIO;

/**
 *
 * @author B Ricks, PhD <bricks@unomaha.edu>
 */
public class Processor {

    /**
     * Process a command
     *
     * @param command The command to process
     * @param file The file to process the command on
     * @param arguments A hashmap of the arguments passed with the command
     * @return The filename of the resulting temp image on success, null
     * otherwise
     */
    public byte[] Process(String command, String file, HashMap<String, String> arguments) {
        //Generate a new temp file name
        String filename = "" + Math.random() + ".png";
        try {
            //Read the original image
            BufferedImage bi = ImageIO.read(new File(file));

            int width = bi.getWidth();
            int height = bi.getHeight();

            //Generate a new image in memory
            BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            //Choose the right method
            if (command.equals("grayscale")) {
                grayscale(bi, out);
            } else if (command.equals("monochrome")) {
                monochrome(bi, out);
            } else if (command.equals("edges")) {
                edges(bi, out);
            } else if (command.equals("reduceColor")) {
                reduceColor(bi, out, arguments);
            } else if (command.equals("histograms")) {
                out = histograms(bi);
            } else if (command.equals("negateImage")) {
                negateImage(bi, out);
            } else if (command.equals("horizontalflip")){
                horizontalflip(bi, out);
            } else if (command.equals("rotate")) {
                out = rotate(bi);
            } else if (command.equals("opaqueshadow")){
                out = opaqueshadow(bi);
            }
            else {
                return null;
            }

            //Write the image
            //ImageIO.write(out, "png", new File(filename));
            //return "\\" + filename;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            ImageIO.write(out, "png", baos);

            return baos.toByteArray();

        } catch (IOException ex) {
            Logger.getLogger(StarterSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    /**
     * Negates the colors of the image.
     * @param bi The buffered image to be negated
     * @param out The negated image
     */
    private void negateImage(BufferedImage bi, BufferedImage out) {
        for (int y = 0; y < bi.getHeight(); y++){
            for (int x = 0; x < bi.getWidth(); x++) {
                Color pixel = new Color(bi.getRGB(x, y));
                int r = 255 - pixel.getRed();
                int g = 255 - pixel.getGreen();
                int b = 255 - pixel.getBlue();
                
                
                r = clamp255(r);
                g = clamp255(g);
                b = clamp255(b);
                
                Color newColor = new Color(r, g, b);
                out.setRGB(x, y, newColor.getRGB());
            }
        }
            
    }
    
    private BufferedImage opaqueshadow(BufferedImage bi) {
        BufferedImage shadow = null;
        
        try {
            shadow = ImageIO.read(new File("SombraSkull.png"));
        } catch (IOException e){
            return bi;
        }

        int shadowWidth = shadow.getWidth();
        int shadowHeight = shadow.getHeight();
        
        int biWidth = bi.getWidth();
        int biHeight = bi.getHeight();
        
        BufferedImage output = new BufferedImage(biWidth, biHeight, BufferedImage.TYPE_4BYTE_ABGR);
        
        if(shadowWidth > biWidth || shadowHeight > biHeight)
        {
            return bi;
        }
        
        for(int j = 0; j < biHeight; j++) {
            for(int i = 0; i < biWidth; i++) {
                Color bipixel = new Color(bi.getRGB(i, j));
                if(j < shadowHeight && i < shadowWidth) {
                    Color pixel = new Color(shadow.getRGB(i, j));
                    if(pixel.getAlpha() > 20) {
                        output.setRGB(i, j, bipixel.darker().getRGB());
                    } else {
                        output.setRGB(i, j, bipixel.getRGB());
                    }
                } else {
                output.setRGB(i, j, bipixel.getRGB());
                }
            }
        }
        
        /*for(int j = 0; j < shadowHeight; j++) {
            for(int i = 0; i < shadowWidth; i++) {
                Color pixel = new Color(shadow.getRGB(i, j));
                if(pixel.getTransparency() <= Transparency.OPAQUE)
                {
                    output.setRGB(i,j, clamp255(output.getRGB(i, j)+ 20));
                }
            }
        }*/
       
        return output;
    }
    /**
     * 
     * This method is suppose to flip the image by flipping the bounds to negative numbers.
     * @param bi the buffered image to be flipped.
     */
    private void horizontalflip(BufferedImage bi, BufferedImage out) {
        
        int height = bi.getHeight();
        int width = bi.getWidth();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixel = new Color(bi.getRGB(x, y));
                int r = pixel.getRed();
                int b = pixel.getBlue();
                int g = pixel.getGreen();
                
                Color temp = new Color(r,g,b);
                out.setRGB((width - 1) - x, y, temp.getRGB());
            }
        }
    }
    /*
    private BufferedImage arbitraryRotation(BufferedImage bi) {
        float rotation = 45;
        BufferedImage fin = new BufferedImage(?, ?, BufferedImage.TYPE_INT_ARGB);
        
        List<Point> points = new ArrayList<Point>();
        List<Point> newPoints = new ArrayList<Point>();
        
        int w = bi.getWidth();
        int h = bi.getHeight();
        
        points.add(new Point (-w/2, h/2));
        points.add(new Point (w/2, h/2));
        points.add(new Point (-w/2, -h/2));
        points.add(new Point (w/2, -h/2));
        
        for (int i = 0; i < points.size(); i++)
        {
            Point p = points.get(i);
            double hypotenus = Math.sqrt(p.x * p.x + p.y * p.y);
            double angle = Math.atan2(p.y, p.x);
            
            double newAngle = angle + rotation;
            
            double newX = Math.cos(newAngle) * hypotenus;
            double newY = Math.sin(newAngle) * hypotenus;
            
            newPoints.add(new Point((int) newX, (int)newY));
        }
        
        int minX = newPoints.stream().min((p1,p2)->Integer.compare(p1.x, p2.x)).get().x;
        int minY = newPoints.stream().min((p1,p2)->Integer.compare(p1.y, p2.y)).get().y;
        int maxX = newPoints.stream().max((p1,p2)->Integer.compare(p1.x, p2.x)).get().x;
        int maxY = newPoints.stream().max((p1,p2)->Integer.compare(p1.y, p2.y)).get().y;
        
        for(int y = 0; y < bi.getHeight(); y++) {
            for(int x = 0; x < bi.getWidth(); x++) {
                Color inColor = new Color(bi.getRGB(x, y));
                
                fin.setRGB(?, ?, inColor);
            }
        }
    }
    */
    private BufferedImage rotate(BufferedImage bi) {
        int height = bi.getHeight();
        int width = bi.getWidth();
        
        BufferedImage fun = new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB);
        
        for(int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixel = new Color(bi.getRGB(x, y));
                int r = pixel.getRed();
                int b = pixel.getBlue();
                int g = pixel.getGreen();
                
                Color temp = new Color(r,g,b);
                fun.setRGB((height - 1) - y, (width - 1) - x, temp.getRGB());
            }
        }
        return fun;
    }
    /**
     * Turn a color image into grayscale
     *
     * @param bi The original image
     * @param out The modified image
     */
    private void grayscale(BufferedImage bi, BufferedImage out) {
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                Color pixel = new Color(bi.getRGB(x, y));

                int r = pixel.getRed();
                int g = pixel.getGreen();
                int b = pixel.getBlue();

                //Set to grayscale
                r = g;
                b = g;

                //Prevent an exception by keeping values within [0,255]
                r = clamp255(r);
                g = clamp255(g);
                b = clamp255(b);

                Color newColor = new Color(r, g, b);

                out.setRGB(x, y, newColor.getRGB());

            }
        }
    }

    /**
     * The list of commands we accept
     *
     * @return The list of commands we accept
     */
    public String[] validCommands() {
        return new String[]{"edges", "histograms", "opaqueshadow", "reduceColor", "grayscale", "monochrome", "negateImage", "horizontalflip", "rotate"};
    }

    /**
     * Keep an int value within 0 and 255
     *
     * @param i The value to clamp
     * @return a number between 0 and 255 inclusively
     */
    private int clamp255(int i) {
        if (i < 0) {
            return 0;
        }
        if (i >= 255) {
            return 255;
        }
        return i;
    }

    /**
     * Keep an float between 0 and 1
     *
     * @param f The float to clamp
     * @return a float between 0 and 1 inclusively
     */
    private float clamp1(float f) {
        if (f <= 0) {
            return 0;
        }
        if (f >= 1) {
            return 1;
        }
        return f;
    }

    /**
     * Convert an image to monochrome.
     *
     * @param bi The original image
     * @param out The modified image
     */
    private void monochrome(BufferedImage bi, BufferedImage out) {
        int width = bi.getWidth();
        int height = bi.getHeight();

        //An array to store the error for each pixel so it can accumulate
        int[][] error = new int[width][height];

        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {
                Color pixel = new Color(bi.getRGB(x, y));

                int r = pixel.getRed();
                int g = pixel.getGreen();
                int b = pixel.getBlue();

                int grayscale = r + error[x][y];

                int newgrayscale = 0;

                if (grayscale > 128) {
                    newgrayscale = 255;
                } else {
                    newgrayscale = 0;
                }

                int totalerror = grayscale - newgrayscale;

                tryadd(error, totalerror / 2, width, height, x + 1, y);
                tryadd(error, 3 * totalerror / 16, width, height, x + 1, y + 1);
                tryadd(error, totalerror / 4, width, height, x, y + 1);
                tryadd(error, totalerror / 16, width, height, x - 1, y + 1);

                /**
                 * if grayscale was 128, newgrayscale would be 0 So error would
                 * be 128. So a positive error means the next pixel should be...
                 */
                r = newgrayscale;
                g = newgrayscale;
                b = newgrayscale;

                //Prevent an exception by keeping values within [0,255]
                r = clamp255(r);
                g = clamp255(g);
                b = clamp255(b);

                Color newColor = new Color(r, g, b);

                out.setRGB(x, y, newColor.getRGB());

            }
        }
    }

    private void reduceColor(BufferedImage bi, BufferedImage out, HashMap<String, String> arguments) {

        int width = bi.getWidth();
        int height = bi.getHeight();

        int maxColors = 8;

        if (arguments.containsKey("maxColors")) {
            maxColors = Integer.parseInt(arguments.get("maxColors"));
        }

        HashMap<Color, Integer> hashMap = new HashMap<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixel = new Color(bi.getRGB(x, y));

                if (hashMap.containsKey(pixel)) {
                    hashMap.put(pixel, hashMap.get(pixel).intValue() + 1);
                } else {
                    hashMap.put(pixel, 1);
                }
            }
        }

        List<ColorIntPair> pairs = new ArrayList<ColorIntPair>();

        Collection<Color> keys = hashMap.keySet();

        for (Color color : keys) {
            int value = hashMap.get(color);

            ColorIntPair cip = new ColorIntPair();
            cip.color = color;
            cip.count = value;
            pairs.add(cip);
        }

        Collections.sort(pairs);
        Collections.reverse(pairs);

        List<Color> possibleColors = new ArrayList<Color>();

        for (int i = 0; i < maxColors && i < pairs.size(); i++) {
            possibleColors.add(randomColor());
        }

        for (int round = 0; round < 10; round++) {

            List<List<ColorIntPair>> votingPairs = new ArrayList<List<ColorIntPair>>();

            for (int i = 0; i < maxColors; i++) {
                votingPairs.add(new ArrayList<ColorIntPair>());
            }

            for (ColorIntPair cip : pairs) {
                //https://stackoverflow.com/questions/36775518/java-stream-find-an-element-with-a-min-max-value-of-an-attribute
                Color closest = possibleColors.stream().reduce((a, b) -> L1Distance(a, cip.color) < L1Distance(b, cip.color) ? a : b).get();
                votingPairs.get(possibleColors.indexOf(closest)).add(cip);

            }

            //So now I have a list of each color pair and all the colors that were closest to that color and how many instances of that color I have
            for (int i = 0; i < maxColors; i++) {
                long r = 0, g = 0, b = 0, count = 0;

                for (ColorIntPair cip : votingPairs.get(i)) {
                    r += cip.color.getRed() * cip.count;
                    g += cip.color.getGreen() * cip.count;
                    b += cip.color.getBlue() * cip.count;
                    count += cip.count;

                }

                if (count > 0) {

                    r /= count;
                    g /= count;
                    b /= count;

                    r = clamp255((int) r);
                    g = clamp255((int) g);
                    b = clamp255((int) b);

                    possibleColors.set(i, new Color((int) r, (int) g, (int) b));
                } else {
                    possibleColors.set(i, randomColor());
                }
            }
        }

        //An array to store the error for each pixel so it can accumulate
        int[][] error = new int[width][height];

        for (int y = 0; y < height; y++) {

            for (int x = 0; x < width; x++) {
                Color pixel = new Color(bi.getRGB(x, y));

                int r = pixel.getRed();
                int g = pixel.getGreen();
                int b = pixel.getBlue();

                Color closestColor = null;
                float closestDistance = Float.MAX_VALUE;

                /*for(int i = 0; i < possibleColors.length; i++)
                {
                    Color color = possibleColors[i];
                }*/
                //foreach
                for (Color color : possibleColors) {
                    float distance = getDistanceL1(new Color(r, g, b), color);
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestColor = color;
                    }
                }

                r = closestColor.getRed();
                g = closestColor.getGreen();
                b = closestColor.getBlue();

                /*int grayscale = r + error[x][y]; 
                
                int newgrayscale = 0;
                                
                if(grayscale > 128 )
                    newgrayscale = 255;
                else
                    newgrayscale = 0;
                
                int totalerror = grayscale - newgrayscale;
                
                tryadd(error, totalerror/2, width, height, x+1, y);
                tryadd(error, 3*totalerror/16, width, height, x+1, y+1);
                tryadd(error, totalerror/4, width, height, x, y+1);
                tryadd(error, totalerror/16, width, height, x-1, y+1);
                
               
                
                
                
                
                
                r = newgrayscale;
                g = newgrayscale;
                b = newgrayscale;*/
                //Prevent an exception by keeping values within [0,255]
                r = clamp255(r);
                g = clamp255(g);
                b = clamp255(b);

                Color newColor = new Color(r, g, b);

                out.setRGB(x, y, newColor.getRGB());

            }
        }
    }

    /**
     * Try to add a value to an array if the x and y values are valid
     *
     * @param error The array to add to
     * @param remainingError The value to add
     * @param width The length of the first dimension
     * @param height The length of the second dimension
     * @param x The index of the first dimension
     * @param y The index of the second dimension
     * @return True of a value was update, false otherwise
     */
    private boolean tryadd(int[][] error, int remainingError, int width, int height, int x, int y) {
        if (x < 0 || x >= width) {
            return false;
        }
        if (y < 0 || y >= height) {
            return false;
        }

        error[x][y] += remainingError;

        return true;

    }

    private float getDistanceL1(Color color, Color color0) {
        return Math.abs(color.getRed() - color0.getRed())
                + Math.abs(color.getGreen() - color0.getGreen())
                + Math.abs(color.getBlue() - color0.getBlue());

    }

    private BufferedImage histograms(BufferedImage bi) {

        BufferedImage convolution = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);

        edges(bi, convolution);

        bi = convolution;

        int marginSize = 20;
        int height = bi.getHeight();
        int width = bi.getWidth();

        BufferedImage withMargins = new BufferedImage(bi.getWidth() + 20, bi.getHeight() + 20, BufferedImage.TYPE_INT_ARGB);

        Graphics graphics = withMargins.getGraphics();

        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, withMargins.getWidth(), withMargins.getHeight());

        graphics.drawImage(bi, marginSize, marginSize, null);

        //Create the histograms of brightness histogram
        for (int y = 0; y < height; y++) {

            int sum = 0;
            for (int x = 0; x < width; x++) {

                Color pixel = new Color(bi.getRGB(x, y));
                int r = pixel.getRed();
                int g = pixel.getGreen();
                int b = pixel.getBlue();

                int grayscale = (int) (r * .3 + g * .4 + b * .3);
                sum += grayscale;

            }

            int marginAmount = (int) (sum / (width * 255.0) * marginSize);
            graphics.setColor(Color.WHITE);
            graphics.drawLine(0, marginSize + y, marginAmount, marginSize + y);

        }

        for (int x = 0; x < width; x++) {
            int sum = 0;
            for (int y = 0; y < height; y++) {

                Color pixel = new Color(bi.getRGB(x, y));
                int r = pixel.getRed();
                int g = pixel.getGreen();
                int b = pixel.getBlue();

                int grayscale = (int) (r * .3 + g * .4 + b * .3);
                sum += grayscale;

            }

            int marginAmount = (int) (sum / (height * 255.0) * marginSize);
            graphics.setColor(Color.WHITE);
            graphics.drawLine(marginSize + x, 0, marginSize + x, marginAmount);

        }

        graphics.dispose();

        return withMargins;

    }

    private Color randomColor() {
        return new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
    }

    private int L1Distance(Color pc, Color color) {
        return Math.abs(pc.getRed() - color.getRed()) + Math.abs(pc.getGreen() - color.getGreen()) + Math.abs(pc.getBlue() - color.getBlue());
    }

    /*
    Deprecated
     */
    private void edges2(BufferedImage bi, BufferedImage out) {
        for (int y = 0; y < bi.getHeight() - 1; y++) {
            for (int x = 0; x < bi.getWidth() - 1; x++) {
                Color pixel = new Color(bi.getRGB(x, y));

                int r = pixel.getRed();
                int g = pixel.getGreen();
                int b = pixel.getBlue();

                Color nextPixel = new Color(bi.getRGB(x + 1, y));

                int nextR = nextPixel.getRed();
                int nextG = nextPixel.getGreen();
                int nextB = nextPixel.getBlue();

                //big differences map to white, small differences map to black
                int differenceSum = Math.abs(r - nextR) + Math.abs(g - nextG) + Math.abs(b - nextB);

                //If the colors are total opposites, what's the max differenceSum can be?
                ///Biggest contrast between white and black -> 255*3=765
                differenceSum /= 1;

                r = differenceSum;
                g = differenceSum;
                b = differenceSum;

                //Prevent an exception by keeping values within [0,255]
                r = clamp255(r);
                g = clamp255(g);
                b = clamp255(b);

                Color newColor = new Color(r, g, b);

                out.setRGB(x, y, newColor.getRGB());

            }
        }
    }

    private void edges(BufferedImage bi, BufferedImage out) {

        int kernelWidth = 5;

        float[][] kernel = new float[kernelWidth][kernelWidth];

//        kernel[0][0] = -1; kernel[1][0] = -1; kernel[2][0] = -1; //Identity kernel
//        kernel[0][1] = -1; kernel[1][1] = 8; kernel[2][1] = -1; //Identity kernel
//        kernel[0][2] = -1; kernel[1][2] = -1; kernel[2][2] = -1; //Identity kernel
//        kernel[0][0] = 0; kernel[1][0] = 0; kernel[2][0] = 0; //Identity kernel
//        kernel[0][1] = 0; kernel[1][1] = 1f; kernel[2][1] = 0f; //Identity kernel
//        kernel[0][2] = 0; kernel[1][2] = 0f; kernel[2][2] = 0; //Identity kernel
//        
//        float kernelCoefficient = .1f;
//        
//        kernel[0][0] += -1 * kernelCoefficient; kernel[1][0] += -1 * kernelCoefficient; kernel[2][0] += -1 * kernelCoefficient; //Identity kernel
//        kernel[0][1] += -1 * kernelCoefficient; kernel[1][1] += 8 * kernelCoefficient; kernel[2][1] += -1 * kernelCoefficient; //Identity kernel
//        kernel[0][2] += -1 * kernelCoefficient; kernel[1][2] += -1 * kernelCoefficient; kernel[2][2] += -1 * kernelCoefficient;        
//        
//3 -> 1, 5 -> 2, 7 -> 3
        //IdentityKernel(kernelWidth, kernel);
        
        BoxBlur(kernelWidth, kernel);

        for (int y = kernelWidth/2; y < bi.getHeight() - kernelWidth/2; y++) {
            for (int x = kernelWidth/2; x < bi.getWidth() - kernelWidth/2; x++) {
                //Color pixel = new Color(bi.getRGB(x, y));

                float r = 0;
                float g = 0;
                float b = 0;

                for (int yk = 0; yk <= kernelWidth - 1; yk++) {
                    for (int xk = 0; xk <= kernelWidth - 1; xk++) {

                        Color nextPixel = new Color(bi.getRGB(x + xk - kernelWidth/2, y + yk - kernelWidth/2));

                        float coefficient = kernel[xk][yk];

                        r += nextPixel.getRed() * coefficient;
                        g += nextPixel.getGreen() * coefficient;
                        b += nextPixel.getBlue() * coefficient;
                    }
                }

                //Prevent an exception by keeping values within [0,255]
                r = abs255(r);
                g = abs255(g);
                b = abs255(b);

                Color newColor = new Color((int) r, (int) g, (int) b);

                out.setRGB(x, y, newColor.getRGB());

            }
        }
    }

    private void BoxBlur(int kernelWidth, float[][] kernel) {
        for (int yk = 0; yk <= kernelWidth - 1; yk++) {
            for (int xk = 0; xk <= kernelWidth - 1; xk++) {
                
                
                kernel[xk][yk] = 1.0f/(kernelWidth * kernelWidth);
                
                
            }
            
        }
    }

    private void IdentityKernel(int kernelWidth, float[][] kernel) {
        //Box blur across 5x5 = 25 values, each cell = 1/25
        for (int yk = 0; yk <= kernelWidth - 1; yk++) {
            for (int xk = 0; xk <= kernelWidth - 1; xk++) {
                
                int middleIndex = kernelWidth / 2;
                
                if (yk == middleIndex && xk == middleIndex) {
                    kernel[xk][yk] = 1;
                } else {
                    kernel[xk][yk] = 0;
                }
                
            }
            
        }
    }

    private int abs255(float color) {
        return clamp255((int) Math.abs(color));
    }

}
