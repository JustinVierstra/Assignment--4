package a4;

import java.nio.*;
import java.lang.Math;
import java.util.Random;

import javax.swing.*;

import static com.jogamp.opengl.GL4.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GLContext;
import org.joml.*;

import java.util.Vector.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Code extends JFrame implements GLEventListener, KeyListener
{	private GLCanvas myCanvas;
	private double startTime = 0.0;
	private double elapsedTime;
	private int renderingProgram, renderingProgramCubeMap, renderingProgramLights, renderingProgramTess, renderingProgram3D;
	private int renderingProgramFLOOR, renderingProgramSURFACE;
	private int vao[] = new int[1];
	private int vbo[] = new int[17];
	private float cameraX, cameraHeight, cameraZ;
	private float lightX, lightY, lightZ;
	private Vector3f earthLocation, moonLocation, terrainLocation, dolphinLocation, dolphin3DLocation;
	private boolean axisBoolean = true;
	private double elapsedTimeOld = 0;
	private float timeDiff = 0;

	private int dolphinTexture;
	private int rightTriangleTexture;
	private int xTexture;
	private int yTexture;
	private int zTexture;
	private int testTexture;
	private int skyboxTexture;
	private int sunTexture;
	private int earthTexture;
	private int moonTexture;

	private int squareMoonTexture;
	private int squareMoonHeight;
	private int squareMoonNormalMap;

	private int[] bufferId = new int[1];
	private int refractTextureId;
	private int reflectTextureId;
	private int refractFrameBuffer;
	private int reflectFrameBuffer;
	
	private int numObjVertices;
	private ImportedModel myModel, dolphin3D;
	private Sphere lightSphere;
	private int numLightVerts;
	private Sphere earthSphere;
	private Sphere moon;
	private int numDolphin3DVerts;

	private int noiseHeight = 256;
	private int noiseWidth = 256;
	private int noiseDepth = 256;
	private double[][][] noise = new double[noiseWidth][noiseHeight][noiseDepth];
	private int noiseTexture;
	private Random random = new Random(5);
	private double PI = 3.1415926535;

	private float depthLookup = 0.0f;
	private int dOffsetLoc;
	private long lastTime = System.currentTimeMillis();

	private float tessInner = 30.0f;
	private float tessOuter = 20.0f;

	private int lightingOff = 0;
	
	// allocate variables for display() function
	private FloatBuffer vals = Buffers.newDirectFloatBuffer(16);
	private Matrix4f pMat = new Matrix4f();  // perspective matrix
	private Matrix4f vMat = new Matrix4f();  // view matrix
	private Matrix4f mMat = new Matrix4f();  // model matrix
	private Matrix4f invTrMat = new Matrix4f(); // inverse-transpose
	private Matrix4f mvpMat = new Matrix4f(); // model-view-perspective matrix
	private Matrix4f mvMat = new Matrix4f(); // model-view matrix
	private int mvpLoc;
	private int mLoc, vLoc, pLoc, nLoc, aboveLoc;
	private int globalAmbLoc, ambLoc, diffLoc, specLoc, posLoc, mambLoc, mdiffLoc, mspecLoc, mshiLoc, mvLoc;
	private float[] thisAmb, thisDif, thisSpe, matAmb, matDif, matSpe;
	private float thisShi, matShi;
	private float aspect;
	private double tf;
	private float amt = 0.0f;
	private int count;

	private int stripeTexture;
	private int texWidth = 200;
	private int texHeight= 200;
	private int texDepth = 200;
	private double[][][] tex3Dpattern = new double[texWidth][texHeight][texDepth];

	private Camera cameraPosition;
	
	//private Vector3f initialLightLoc = new Vector3f(2.0f, 5.0f, -12.0f);
	private Vector3f initialLightLoc = new Vector3f(-10.0f, 10.0f, -50.0f);
	
	private int width = 800, height = 800;

	private float surfacePlaneHeight = 0.0f;
	private float floorPlaneHeight = -10.0f;
	
	private float[] lightPos = new float[3];
	private Vector3f currentLightPos = new Vector3f();

	// white light properties
	// float[] globalAmbient = new float[] { 0.6f, 0.6f, 0.6f, 1.0f };
	// float[] lightAmbient = new float[] { 0.1f, 0.1f, 0.1f, 1.0f };
	// float[] lightDiffuse = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
	// float[] lightSpecular = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };

	float[] globalAmbient = new float[] { 0.7f, 0.7f, 0.7f, 1.0f };
	float[] lightAmbient = new float[] { 0.0f, 0.0f, 0.0f, 1.0f };
	float[] lightDiffuse = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
	float[] lightSpecular = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };

	float[] silverMatAmb = Utils.silverAmbient();
	float[] silverMatDif = Utils.silverDiffuse();
	float[] silverMatSpe = Utils.silverSpecular();
	float silverMatShi = Utils.silverShininess();

	// gold material
	// private float[] goldMatAmb = Utils.goldAmbient();
	// private float[] goldMatDif = Utils.goldDiffuse();
	// private float[] goldMatSpe = Utils.goldSpecular();
	// private float goldMatShi = Utils.goldShininess();
	private float[] goldMatAmb = new float[] { 0.5f, 0.6f, 0.8f, 1.0f };
	private float[] goldMatDif = new float[] { 0.8f, 0.9f, 1.0f, 1.0f };
	private float[] goldMatSpe = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
	private float goldMatShi = 250.0f;
	
	// bronze material
	private float[] bronzeMatAmb = Utils.bronzeAmbient();
	private float[] bronzeMatDif = Utils.bronzeDiffuse();
	private float[] bronzeMatSpe = Utils.bronzeSpecular();
	private float bronzeMatShi = Utils.bronzeShininess();

	public Code()
	{	setTitle("Assignment #4");
		setSize(800, 800);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		this.add(myCanvas);
		this.setVisible(true);
		Animator animator = new Animator(myCanvas);
		animator.start();
		addKeyListener(this);
		myCanvas.addKeyListener(this);
	}

	// 3D Noise Texture section

	private double smooth(double zoom, double x1, double y1, double z1)
	{	//get fractional part of x, y, and z
		double fractX = x1 - (int) x1;
		double fractY = y1 - (int) y1;
		double fractZ = z1 - (int) z1;

		//neighbor values that wrap
		double x2 = x1 - 1; if (x2<0) x2 = (Math.round(noiseWidth / zoom)) - 1;
		double y2 = y1 - 1; if (y2<0) y2 = (Math.round(noiseHeight / zoom)) - 1;
		double z2 = z1 - 1; if (z2<0) z2 = (Math.round(noiseDepth / zoom)) - 1;

		//smooth the noise by interpolating
		double value = 0.0;
		value += fractX       * fractY       * fractZ       * noise[(int)x1][(int)y1][(int)z1];
		value += (1.0-fractX) * fractY       * fractZ       * noise[(int)x2][(int)y1][(int)z1];
		value += fractX       * (1.0-fractY) * fractZ       * noise[(int)x1][(int)y2][(int)z1];	
		value += (1.0-fractX) * (1.0-fractY) * fractZ       * noise[(int)x2][(int)y2][(int)z1];
				
		value += fractX       * fractY       * (1.0-fractZ) * noise[(int)x1][(int)y1][(int)z2];
		value += (1.0-fractX) * fractY       * (1.0-fractZ) * noise[(int)x2][(int)y1][(int)z2];
		value += fractX       * (1.0-fractY) * (1.0-fractZ) * noise[(int)x1][(int)y2][(int)z2];
		value += (1.0-fractX) * (1.0-fractY) * (1.0-fractZ) * noise[(int)x2][(int)y2][(int)z2];
		
		return value;
	}

	private double turbulence(double x, double y, double z, double maxZoom)
	{	double sum = 0.0, zoom = maxZoom;
	
		sum = (Math.sin((1.0/512.0)*(8*PI)*(x+z)) + 1) * 8.0;
		while(zoom >= 0.9)
		{	sum = sum + smooth(zoom, x/zoom, y/zoom, z/zoom) * zoom;
			zoom = zoom / 2.0;
		}
		sum = 128.0 * sum/maxZoom;
		return sum;
	}

	private void fillDataArray(byte data[])
	{	double maxZoom = 32.0;
		for (int i=0; i<noiseWidth; i++)
		{	for (int j=0; j<noiseHeight; j++)
			{	for (int k=0; k<noiseDepth; k++)
				{	noise[i][j][k] = random.nextDouble();
		}	}	}
		for (int i = 0; i<noiseHeight; i++)
		{	for (int j = 0; j<noiseWidth; j++)
			{	for (int k = 0; k<noiseDepth; k++)
				{	data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+0] = (byte)turbulence(i,j,k,maxZoom);
					data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+1] = (byte)turbulence(i,j,k,maxZoom);
					data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+2] = (byte)turbulence(i,j,k,maxZoom);
					data[i*(noiseWidth*noiseHeight*4)+j*(noiseHeight*4)+k*4+3] = (byte)255;
	}	}	}	}

	private int buildNoiseTexture()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();

		byte[] data = new byte[noiseWidth*noiseHeight*noiseDepth*4];
		
		fillDataArray(data);

		ByteBuffer bb = Buffers.newDirectByteBuffer(data);

		int[] textureIDs = new int[1];
		gl.glGenTextures(1, textureIDs, 0);
		int textureID = textureIDs[0];

		gl.glBindTexture(GL_TEXTURE_3D, textureID);

		gl.glTexStorage3D(GL_TEXTURE_3D, 1, GL_RGBA8, noiseWidth, noiseHeight, noiseDepth);
		gl.glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, 0,
				noiseWidth, noiseHeight, noiseDepth, GL_RGBA, GL_UNSIGNED_BYTE, bb);
	
		gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		return textureID;
	}

	// 3D Texture section

	private void fillDataArray3D(byte data[])
	{ for (int i=0; i<texWidth; i++)
		{ for (int j=0; j<texHeight; j++)
		{ for (int k=0; k<texDepth; k++)
			{
		if (tex3Dpattern[i][j][k] == 1.0)
		{	// yellow color
			data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+0] = (byte) 255; //red
			data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+1] = (byte) 255; //green
			data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+2] = (byte) 0;   //blue
			data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+3] = (byte) 0;   //alpha
		}
		else
		{	// blue color
			data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+0] = (byte) 0;   //red
			data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+1] = (byte) 0;   //green
			data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+2] = (byte) 255; //blue
			data[i*(texWidth*texHeight*4)+j*(texHeight*4)+k*4+3] = (byte) 0;   //alpha
		}
	} } } }

	private void createReflectRefractBuffers()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		// Initialize Reflect Framebuffer
		gl.glGenFramebuffers(1, bufferId, 0);
		reflectFrameBuffer = bufferId[0];
		gl.glBindFramebuffer(GL_FRAMEBUFFER, reflectFrameBuffer);
		gl.glGenTextures(1, bufferId, 0);
		reflectTextureId = bufferId[0];
		gl.glBindTexture(GL_TEXTURE_2D, reflectTextureId);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, myCanvas.getWidth(), myCanvas.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, reflectTextureId, 0);
		gl.glDrawBuffer(GL_COLOR_ATTACHMENT0);
		gl.glGenTextures(1, bufferId, 0);
		gl.glBindTexture(GL_TEXTURE_2D, bufferId[0]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24, myCanvas.getWidth(), myCanvas.getHeight(), 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, bufferId[0], 0);

		// Initialize Refract Framebuffer
		gl.glGenFramebuffers(1, bufferId, 0);
		refractFrameBuffer = bufferId[0];
		gl.glBindFramebuffer(GL_FRAMEBUFFER, refractFrameBuffer);
		gl.glGenTextures(1, bufferId, 0);
		refractTextureId = bufferId[0];
		gl.glBindTexture(GL_TEXTURE_2D, refractTextureId);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, myCanvas.getWidth(), myCanvas.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, refractTextureId, 0);
		gl.glDrawBuffer(GL_COLOR_ATTACHMENT0);
		gl.glGenTextures(1, bufferId, 0);
		gl.glBindTexture(GL_TEXTURE_2D, bufferId[0]);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24, myCanvas.getWidth(), myCanvas.getHeight(), 0, GL_DEPTH_COMPONENT, GL_FLOAT, null);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, bufferId[0], 0);
	}

	private void prepForSkyBoxRender()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();

		gl.glUseProgram(renderingProgramCubeMap);

		vLoc = gl.glGetUniformLocation(renderingProgramCubeMap, "v_matrix");
		pLoc = gl.glGetUniformLocation(renderingProgramCubeMap, "p_matrix");
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_CUBE_MAP, skyboxTexture);
	}

	private void prepForTopSurfaceRender()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();

		gl.glUseProgram(renderingProgramSURFACE);

		mLoc = gl.glGetUniformLocation(renderingProgramSURFACE, "m_matrix");
		vLoc = gl.glGetUniformLocation(renderingProgramSURFACE, "v_matrix");
		pLoc = gl.glGetUniformLocation(renderingProgramSURFACE, "p_matrix");
		nLoc = gl.glGetUniformLocation(renderingProgramSURFACE, "norm_matrix");
		aboveLoc = gl.glGetUniformLocation(renderingProgramSURFACE, "isAbove");

		mMat.translation(0.0f, surfacePlaneHeight, 0.0f);
		//mMat.rotateXYZ((float)Math.toRadians(cameraPosition.getYaw()), (float)Math.toRadians(cameraPosition.getPitch()), (float)Math.toRadians(cameraPosition.getRoll()));

		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);

		//currentLightPos.set(initialLightLoc);
		installLights(renderingProgramSURFACE);

		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));

		if (cameraPosition.getHeight() >= surfacePlaneHeight)
			gl.glUniform1i(aboveLoc, 1);
		else
			gl.glUniform1i(aboveLoc, 0);

		dOffsetLoc = gl.glGetUniformLocation(renderingProgramSURFACE, "depthOffset");
		gl.glUniform1f(dOffsetLoc, depthLookup);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[13]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, reflectTextureId);
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, refractTextureId);
		gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_3D, noiseTexture);
	}

	private void prepForFloorRender()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();

		gl.glUseProgram(renderingProgramFLOOR);

		mLoc = gl.glGetUniformLocation(renderingProgramFLOOR, "m_matrix");
		vLoc = gl.glGetUniformLocation(renderingProgramFLOOR, "v_matrix");
		pLoc = gl.glGetUniformLocation(renderingProgramFLOOR, "p_matrix");
		nLoc = gl.glGetUniformLocation(renderingProgramFLOOR, "norm_matrix");
		aboveLoc = gl.glGetUniformLocation(renderingProgramFLOOR, "isAbove");
		
		mMat.translation(0.0f, floorPlaneHeight, 0.0f);

		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);

		//currentLightPos.set(initialLightLoc);

		// gold material
		matAmb = new float[] { 0.5f, 0.6f, 0.8f, 1.0f };
		matDif = new float[] { 0.8f, 0.9f, 1.0f, 1.0f };
		matSpe = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
		matShi = 250.0f;

		installLights(renderingProgramFLOOR);

		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));

		if (cameraPosition.getHeight() >= surfacePlaneHeight)
			gl.glUniform1i(aboveLoc, 1);
		else
			gl.glUniform1i(aboveLoc, 0);

		dOffsetLoc = gl.glGetUniformLocation(renderingProgramFLOOR, "depthOffset");
		gl.glUniform1f(dOffsetLoc, depthLookup);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[13]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_3D, noiseTexture);
	}

	public void display(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);

		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.identity().setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);


		// mMat.identity();
		// vMat.identity().setTranslation(dolphinLocation);
		// vMat.rotateXYZ((float)Math.toRadians(cameraPosition.getYaw()), (float)Math.toRadians(cameraPosition.getPitch()), (float)Math.toRadians(cameraPosition.getRoll()));

		try {
			elapsedTimeOld = (float) elapsedTime;
			elapsedTime = System.currentTimeMillis() - startTime;
			timeDiff = (float) elapsedTime - (float) elapsedTimeOld;
			tf = elapsedTime/1000.0;  // time factor
		} catch (Exception e) {}

		elapsedTime = System.currentTimeMillis() - startTime;

		long currentTime = System.currentTimeMillis();
		elapsedTime = currentTime - lastTime;
		lastTime = currentTime;
		
		depthLookup += (float)elapsedTime * .0001f;

		// render reflection scene to reflection buffer ----------------
		
		if (cameraPosition.getHeight() >= surfacePlaneHeight)
		{	vMat.translation(cameraPosition.getX(), -(surfacePlaneHeight - cameraPosition.getHeight()), cameraPosition.getZ());

			vMat.rotateXYZ((float)Math.toRadians(cameraPosition.getYaw()), (float)Math.toRadians(cameraPosition.getPitch()), (float)Math.toRadians(cameraPosition.getRoll()));

			gl.glBindFramebuffer(GL_FRAMEBUFFER, reflectFrameBuffer);
			gl.glClear(GL_DEPTH_BUFFER_BIT);
			gl.glClear(GL_COLOR_BUFFER_BIT);
			prepForSkyBoxRender();
			gl.glEnable(GL_CULL_FACE);
			gl.glFrontFace(GL_CCW);	// cube is CW, but we are viewing the inside
			gl.glDisable(GL_DEPTH_TEST);
			gl.glDrawArrays(GL_TRIANGLES, 0, 36);
			gl.glEnable(GL_DEPTH_TEST);
		}

		// render refraction scene to refraction buffer ----------------------------------------

		vMat.translation(-cameraPosition.getX(), -(-surfacePlaneHeight - cameraPosition.getHeight()), -cameraPosition.getZ());
		//vMat.rotateY((float)Math.toRadians(cameraPosition.getPitch()));
		//vMat.rotateX((float)Math.toRadians(15.0f));
		vMat.rotateXYZ((float)Math.toRadians(cameraPosition.getYaw()), (float)Math.toRadians(cameraPosition.getPitch()), (float)Math.toRadians(cameraPosition.getRoll()));

		gl.glBindFramebuffer(GL_FRAMEBUFFER, refractFrameBuffer);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glClear(GL_COLOR_BUFFER_BIT);
		// neg
		if (cameraPosition.getHeight() >= surfacePlaneHeight)
		{	prepForFloorRender();
			gl.glEnable(GL_DEPTH_TEST);
			gl.glDepthFunc(GL_LEQUAL);
			gl.glDrawArrays(GL_TRIANGLES, 0, 6);
			// test 2
			// System.out.println(cameraPosition.getHeight());
			// System.out.println("SP: " + surfacePlaneHeight);
		}	
		else
		{	prepForSkyBoxRender();
			gl.glEnable(GL_CULL_FACE);
			gl.glFrontFace(GL_CCW);	// cube is CW, but we are viewing the inside
			gl.glDisable(GL_DEPTH_TEST);
			gl.glDrawArrays(GL_TRIANGLES, 0, 36);
			gl.glEnable(GL_DEPTH_TEST);
		}

		// now render the entire scene #####################################

		gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glClear(GL_COLOR_BUFFER_BIT);

		// draw cube map

		prepForSkyBoxRender();
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);	// cube is CW, but we are viewing the inside
		gl.glDisable(GL_DEPTH_TEST);
		gl.glDrawArrays(GL_TRIANGLES, 0, 36);
		gl.glEnable(GL_DEPTH_TEST);

		// gl.glUseProgram(renderingProgram);
		// mLoc = gl.glGetUniformLocation(renderingProgram, "m_matrix");
		// vLoc = gl.glGetUniformLocation(renderingProgram, "v_matrix");
		// pLoc = gl.glGetUniformLocation(renderingProgram, "p_matrix");
		// nLoc = gl.glGetUniformLocation(renderingProgram, "norm_matrix");

		// vMat.identity().setTranslation(cameraPosition.getLocation());
		// vMat.rotateXYZ((float)Math.toRadians(cameraPosition.getYaw()), (float)Math.toRadians(cameraPosition.getPitch()), (float)Math.toRadians(cameraPosition.getRoll()));

		//vMat = cameraPosition.getViewMatrix();
		amt += elapsedTime * 0.01f;

		// Terrain Code

		gl.glUseProgram(renderingProgramTess);

		matAmb = new float[] { 0.0f, 0.9f, 0.2f, 1.0f };
		matDif = new float[] { 0.8f, 0.9f, 1.0f, 1.0f };
		matSpe = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
		matShi = 250.0f;
		
		// elapsedTime = System.currentTimeMillis() - prevTime;
		// prevTime = System.currentTimeMillis();
		//currentLightPos.x = lightLoc.x + (float)elapsedTime * lightMovement;
		//if (lightLoc.x > 0.5) lightMovement = -.0001f;
		//else if (lightLoc.x < -0.5) lightMovement = .0001f;
		
		mLoc = gl.glGetUniformLocation(renderingProgramTess, "m_matrix");
		vLoc = gl.glGetUniformLocation(renderingProgramTess, "v_matrix");
		pLoc = gl.glGetUniformLocation(renderingProgramTess, "p_matrix");
		nLoc = gl.glGetUniformLocation(renderingProgramTess, "norm_matrix");
		
		//vMat.identity().setTranslation(-cameraPosition.getX(), -cameraPosition.getY(), -cameraPosition.getZ());
		
		//mMat.identity().setTranslation(terrainLocation.x(), terrainLocation.y(), terrainLocation.z());
		vMat.identity().setTranslation(cameraPosition.getLocation());
		vMat.rotateXYZ((float)Math.toRadians(cameraPosition.getYaw()), (float)Math.toRadians(cameraPosition.getPitch()), (float)Math.toRadians(cameraPosition.getRoll()));
		//mMat.rotateX((float) Math.toRadians(20.0f));
		//mMat.setTranslation(terrainLocation.x(), terrainLocation.y(), terrainLocation.z());
		mMat.scaling(300f);
		
		//mMat.setTranslation(0f,-100f,0f);
		mMat.setTranslation(terrainLocation.x(), terrainLocation.y(), terrainLocation.z());
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);
		
		//currentLightPos.set(lightLoc);		
		installLights(renderingProgramTess);
		
		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, squareMoonTexture);
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glBindTexture(GL_TEXTURE_2D, squareMoonHeight);
		gl.glActiveTexture(GL_TEXTURE2);
		gl.glBindTexture(GL_TEXTURE_2D, squareMoonNormalMap);
	
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CW);

		gl.glPatchParameteri(GL_PATCH_VERTICES, 4);
		gl.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		gl.glDrawArraysInstanced(GL_PATCHES, 0, 4, 64*64);

		gl.glUseProgram(renderingProgram);
		mLoc = gl.glGetUniformLocation(renderingProgram, "m_matrix");
		vLoc = gl.glGetUniformLocation(renderingProgram, "v_matrix");
		pLoc = gl.glGetUniformLocation(renderingProgram, "p_matrix");
		nLoc = gl.glGetUniformLocation(renderingProgram, "norm_matrix");

		gl.glFrontFace(GL_CCW);

		// vMat.identity().setTranslation(cameraPosition.getLocation());
		// vMat.rotateXYZ((float)Math.toRadians(cameraPosition.getYaw()), (float)Math.toRadians(cameraPosition.getPitch()), (float)Math.toRadians(cameraPosition.getRoll()));

		// vMat.identity().setTranslation(cameraPosition.getLocation());
		//vMat.identity().setTranslation(cameraPosition.getLocation());
		// vMat.rotateXYZ((float)Math.toRadians(cameraPosition.getYaw()), (float)Math.toRadians(cameraPosition.getPitch()), (float)Math.toRadians(cameraPosition.getRoll()));


		// Sun Code
		matAmb = goldMatAmb;
		matDif = goldMatDif;
		matSpe = goldMatSpe;
		matShi = goldMatShi;

		lightingOff = 0;
		int lightsOff = gl.glGetUniformLocation(renderingProgram,"lightsStatus");
		gl.glProgramUniform1i(renderingProgram, lightsOff, lightingOff);
		installLights(renderingProgram);

		mMat.identity();
		mMat.scaling(0.75f, 0.75f, 0.75f);
		//currentLightPos.rotateAxis((float)Math.toRadians(1), 0.0f, 1.0f, 0.0f);
		mMat.translate(currentLightPos);
		//mMat.translate(currentLightPos.x()*-40, currentLightPos.y(),currentLightPos.z());

		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, sunTexture);

		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glDisable(GL_CULL_FACE);
		gl.glFrontFace(GL_CW);
		gl.glDrawArrays(GL_TRIANGLES, 0, numLightVerts);

		// Moon Code
		matAmb = silverMatAmb;
		matDif = silverMatDif;
		matSpe = silverMatSpe;
		matShi = silverMatShi;

		lightingOff = 1;
		lightsOff = gl.glGetUniformLocation(renderingProgram,"lightsStatus");
		gl.glProgramUniform1i(renderingProgram, lightsOff, lightingOff);
		installLights(renderingProgram);

		mMat.identity();
		mMat.scaling(0.35f, 0.35f, 0.35f);
		mMat.translate(earthLocation.x()+3, earthLocation.y()+1,earthLocation.z());

		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, moonTexture);

		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glDisable(GL_CULL_FACE);
		gl.glFrontFace(GL_CW);
		gl.glDrawArrays(GL_TRIANGLES, 0, numLightVerts);

		// Earth Code

		matAmb = silverMatAmb;
		matDif = silverMatDif;
		matSpe = silverMatSpe;
		matShi = silverMatShi;

		lightingOff = 1;
		lightsOff = gl.glGetUniformLocation(renderingProgram,"lightsStatus");
		gl.glProgramUniform1i(renderingProgram, lightsOff, lightingOff);
		installLights(renderingProgram);

		mMat.identity();
		mMat.scaling(0.5f, 0.5f, 0.5f);
		mMat.translate(earthLocation);
		earthLocation.rotateAxis((float)Math.toRadians(0.1), 0.0f, 1.0f, 0.0f);

		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, earthTexture);

		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glDisable(GL_CULL_FACE);
		gl.glFrontFace(GL_CW);
		gl.glDrawArrays(GL_TRIANGLES, 0, numLightVerts);

		// Dolphin Code

		matAmb = silverMatAmb;
		matDif = silverMatDif;
		matSpe = silverMatSpe;
		matShi = silverMatShi;

		installLights(renderingProgram);

		mMat.identity();
		//vMat.identity();
		mMat.scaling(5f,5f,5f);
		//mMat.setTranslation(dolphinLocation);
		//mMat.setTranslation(cameraPosition.getLocation());
		//mMat.rotateXYZ((float)Math.toRadians(cameraPosition.getYaw()), (float)Math.toRadians(cameraPosition.getPitch()), (float)Math.toRadians(cameraPosition.getRoll()));
		mMat.rotateY((float)Math.toRadians(180f));
		installLights(renderingProgram);
		mMat.invert(invTrMat);
		invTrMat.transpose(invTrMat);
		
		//mMat = cameraPosition.getViewMatrix();
		//currentLightPos.set(initialLightLoc);

		//lightX=currentLightPos.x(); lightY=currentLightPos.y(); lightZ=currentLightPos.z();

		// vMat.identity().setTranslation(cameraPosition.getLocation());
		// vMat.rotateXYZ((float)Math.toRadians(cameraPosition.getYaw()), (float)Math.toRadians(cameraPosition.getPitch()), (float)Math.toRadians(cameraPosition.getRoll()));
		// CAMERA STUFF HERE
		//vMat.rotateY((float)Math.toRadians(45.0f));

		
		gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		gl.glUniformMatrix4fv(nLoc, 1, false, invTrMat.get(vals));

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]);
		gl.glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(2);

		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, dolphinTexture);

		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glDisable(GL_CULL_FACE);
		gl.glFrontFace(GL_CW);
		gl.glDrawArrays(GL_TRIANGLES, 0, myModel.getNumVertices());
		
		// Dolphin 3d Texture

		gl.glUseProgram(renderingProgram3D);

		mvLoc = gl.glGetUniformLocation(renderingProgram3D, "mv_matrix");
		pLoc = gl.glGetUniformLocation(renderingProgram3D, "p_matrix");
		
		//vMat.identity().setTranslation(-cameraPosition.getX(),-cameraPosition.getY(),-cameraPosition.getZ());
		//mMat.rotateY((float)Math.toRadians(180f));
		
		mMat.identity();
		mMat.scaling(5f,5f,5f);
		mMat.rotateY((float)Math.toRadians(270f));
		mMat.setTranslation(dolphin3DLocation);
				
		//vMat.identity().setTranslation(-cameraPosition.getX(),-cameraPosition.getY(),-cameraPosition.getZ());
		//mMat.rotateY((float)Math.toRadians(180f));
		// mMat.rotateX((float)Math.toRadians(15.0f));
		// mMat.rotateY((float)Math.toRadians(45.0f));

		mvMat.identity();
		mvMat.mul(vMat);
		mvMat.mul(mMat);

		// gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		// gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		// gl.glUniformMatrix4fv(mLoc, 1, false, mMat.get(vals));
		// gl.glUniformMatrix4fv(vLoc, 1, false, vMat.get(vals));
		// gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));

		gl.glUniformMatrix4fv(mvLoc, 1, false, mvMat.get(vals));
		gl.glUniformMatrix4fv(pLoc, 1, false, pMat.get(vals));
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_3D, stripeTexture);
		
		gl.glEnable(GL_CULL_FACE);
		gl.glFrontFace(GL_CCW);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		gl.glDrawArrays(GL_TRIANGLES, 0, myModel.getNumVertices());


		gl.glUseProgram(renderingProgram);
		mLoc = gl.glGetUniformLocation(renderingProgram, "m_matrix");
		vLoc = gl.glGetUniformLocation(renderingProgram, "v_matrix");
		pLoc = gl.glGetUniformLocation(renderingProgram, "p_matrix");
		nLoc = gl.glGetUniformLocation(renderingProgram, "norm_matrix");


		if(axisBoolean){
			lightingOff = 0;
			lightsOff = gl.glGetUniformLocation(renderingProgram,"lightsStatus");
			gl.glProgramUniform1i(renderingProgram, lightsOff, lightingOff);
			// X axis
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);
			gl.glActiveTexture(GL_TEXTURE0);
			gl.glBindTexture(GL_TEXTURE_2D, xTexture);
			gl.glDrawArrays(GL_LINES, 0, 2);

			// Y axis
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);
			gl.glActiveTexture(GL_TEXTURE0);
			gl.glBindTexture(GL_TEXTURE_2D, yTexture);
			gl.glDrawArrays(GL_LINES, 0, 2);
			
			// Z axis
			gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
			gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
			gl.glEnableVertexAttribArray(0);
			gl.glActiveTexture(GL_TEXTURE0);
			gl.glBindTexture(GL_TEXTURE_2D, zTexture);
			gl.glDrawArrays(GL_LINES, 0, 2);
		}

		// draw water top (surface) ======================

		prepForTopSurfaceRender();

		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);

		if (cameraPosition.getHeight() >= surfacePlaneHeight){
			gl.glFrontFace(GL_CCW);
			//System.out.println("here");
		}
		else{
			gl.glFrontFace(GL_CW);
			//System.out.println("here1");
			//System.out.println(cameraPosition.getHeight());
		}
		gl.glDrawArrays(GL_TRIANGLES, 0, 6);

		// draw water bottom (floor) =========================

		prepForFloorRender();
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glFrontFace(GL_CCW);
		gl.glDrawArrays(GL_TRIANGLES, 0, 6);
	}

	public void init(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		startTime = System.currentTimeMillis();
		myModel = new ImportedModel("dolphinHighPoly.obj");
		dolphin3D = new ImportedModel("dolphinHighPoly.obj");
		renderingProgram3D =  renderingProgram = Utils.createShaderProgram("Shaders/vertShader3D.glsl", "Shaders/fragShader3D.glsl");
		renderingProgram = Utils.createShaderProgram("Shaders/vertShader.glsl", "Shaders/fragShader.glsl");
		renderingProgramCubeMap = Utils.createShaderProgram("Shaders/vertCShader.glsl", "Shaders/fragCShader.glsl");
		renderingProgramSURFACE = Utils.createShaderProgram("Shaders/vertShaderSURFACE.glsl", "Shaders/fragShaderSURFACE.glsl");
		renderingProgramFLOOR = Utils.createShaderProgram("Shaders/vertShaderFLOOR.glsl", "Shaders/fragShaderFLOOR.glsl");
		renderingProgramTess = Utils.createShaderProgram("Shaders/vertShaderT.glsl", "Shaders/tessCShader.glsl", "Shaders/tessEShader.glsl", "Shaders/fragShaderT.glsl");

		aspect = (float) myCanvas.getWidth() / (float) myCanvas.getHeight();
		pMat.setPerspective((float) Math.toRadians(60.0f), aspect, 0.1f, 1000.0f);

		setupVertices();

		cameraX = 0.0f; cameraHeight = -2.0f; cameraZ = -40.0f;
		//cameraX = 0f; cameraHeight = -2.0f; cameraZ = 0f;
		lightX = initialLightLoc.x(); lightY = initialLightLoc.y(); lightZ = initialLightLoc.z();

		cameraPosition = new Camera(cameraX, cameraHeight, cameraZ);
		currentLightPos.set(initialLightLoc);
		moonLocation = new Vector3f(lightX-1,lightY,lightZ-1);
		earthLocation = new Vector3f(-5f,2f,-8f);
		terrainLocation = new Vector3f(0f,-2f,0f);
		dolphinLocation = new Vector3f(0f,0f,0f);
		dolphin3DLocation = new Vector3f(13.0f,5.0f,-40.0f);
		//vMat.translate(-cameraX,-cameraHeight,-cameraZ);

		skyboxTexture = Utils.loadCubeMap("cubeMap");
		dolphinTexture = Utils.loadTexture("Dolphin_HighPolyUV.png");
		xTexture = Utils.loadTexture("x.png");
		yTexture = Utils.loadTexture("y.png");
		zTexture = Utils.loadTexture("z.png");
		sunTexture = Utils.loadTexture("sunmap.jpg");
		earthTexture = Utils.loadTexture("earthmap1k.jpg");
		moonTexture = Utils.loadTexture("moonmap4k.jpg");

		squareMoonTexture = Utils.loadTexture("squareMoonMap.jpg");
		squareMoonHeight = Utils.loadTexture("squareMoonBump.jpg");
		squareMoonNormalMap = Utils.loadTexture("squareMoonNormal.jpg");

		// squareMoonTexture = Utils.loadTexture("hills.jpg");
		// squareMoonHeight = Utils.loadTexture("hillsbump.jpg");
		// squareMoonNormalMap = Utils.loadTexture("hillsnorms.jpg");
		//vMat.rotateXYZ((float)Math.toRadians(cameraPosition.getYaw()), (float)Math.toRadians(cameraPosition.getPitch()), (float)Math.toRadians(cameraPosition.getRoll()));
		createReflectRefractBuffers();

		noiseTexture = buildNoiseTexture();

		generate3Dpattern();	
		stripeTexture = load3DTexture();
	}

	private void installLights(int renderingProgram)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		//renderingProgram = renderingProgram;
		lightPos[0]=currentLightPos.x(); lightPos[1]=currentLightPos.y(); lightPos[2]=currentLightPos.z();

		// get the locations of the light and material fields in the shader
		globalAmbLoc = gl.glGetUniformLocation(renderingProgram, "globalAmbient");
		ambLoc = gl.glGetUniformLocation(renderingProgram, "light.ambient");
		diffLoc = gl.glGetUniformLocation(renderingProgram, "light.diffuse");
		specLoc = gl.glGetUniformLocation(renderingProgram, "light.specular");
		posLoc = gl.glGetUniformLocation(renderingProgram, "light.position");
		mambLoc = gl.glGetUniformLocation(renderingProgram, "material.ambient");
		mdiffLoc = gl.glGetUniformLocation(renderingProgram, "material.diffuse");
		mspecLoc = gl.glGetUniformLocation(renderingProgram, "material.specular");
		mshiLoc = gl.glGetUniformLocation(renderingProgram, "material.shininess");
	
		//  set the uniform light and material values in the shader
		gl.glProgramUniform4fv(renderingProgram, globalAmbLoc, 1, globalAmbient, 0);
		gl.glProgramUniform4fv(renderingProgram, ambLoc, 1, lightAmbient, 0);
		gl.glProgramUniform4fv(renderingProgram, diffLoc, 1, lightDiffuse, 0);
		gl.glProgramUniform4fv(renderingProgram, specLoc, 1, lightSpecular, 0);
		gl.glProgramUniform3fv(renderingProgram, posLoc, 1, lightPos, 0);
		gl.glProgramUniform4fv(renderingProgram, mambLoc, 1, matAmb, 0);
		gl.glProgramUniform4fv(renderingProgram, mdiffLoc, 1, matDif, 0);
		gl.glProgramUniform4fv(renderingProgram, mspecLoc, 1, matSpe, 0);
		gl.glProgramUniform1f(renderingProgram, mshiLoc, matShi);
	}

	private void setupVertices(){
		
		GL4 gl = (GL4) GLContext.getCurrentGL();


		// Dolphin
		numObjVertices = myModel.getNumVertices();
		Vector3f[] vertices = myModel.getVertices();
		Vector2f[] texCoords = myModel.getTexCoords();
		Vector3f[] normals = myModel.getNormals();
		
		float[] pvalues = new float[numObjVertices*3];
		float[] tvalues = new float[numObjVertices*2];
		float[] nvalues = new float[numObjVertices*3];
		
		for (int i=0; i<numObjVertices; i++)
		{	pvalues[i*3]   = (float) (vertices[i]).x();
			pvalues[i*3+1] = (float) (vertices[i]).y();
			pvalues[i*3+2] = (float) (vertices[i]).z();
			tvalues[i*2]   = (float) (texCoords[i]).x();
			tvalues[i*2+1] = (float) (texCoords[i]).y();
			nvalues[i*3]   = (float) (normals[i]).x();
			nvalues[i*3+1] = (float) (normals[i]).y();
			nvalues[i*3+2] = (float) (normals[i]).z();
		}

		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(vbo.length, vbo, 0);

		// Planes
		// float[] PLANE_POSITIONS = {
		// 	-120.0f, 0.0f, -240.0f,  -120.0f, 0.0f, 0.0f,  120.0f, 0.0f, -240.0f,
		// 	120.0f, 0.0f, -240.0f,  -120.0f, 0.0f, 0.0f,  120.0f, 0.0f, 0.0f
		// };
		// float[] PLANE_POSITIONS = {
		// 	-240.0f, 0.0f, -240.0f,  -240.0f, 0.0f, 0.0f,  240.0f, 0.0f, -240.0f,
		// 	240.0f, 0.0f, -240.0f,  -240.0f, 0.0f, 0.0f,  240.0f, 0.0f, 0.0f
		// };
		// float[] PLANE_POSITIONS = {
		// 	-240.0f, 0.0f, -240.0f,  -240.0f, 0.0f, 240.0f,  240.0f, 0.0f, -240.0f,
		// 	240.0f, 0.0f, -240.0f,  -240.0f, 0.0f, 240.0f,  240.0f, 0.0f, 240.0f
		// };

		float[] PLANE_POSITIONS = {
			-120.0f, 0.0f, -240.0f,  -120.0f, 0.0f, 0.0f,  120.0f, 0.0f, -240.0f,
			120.0f, 0.0f, -240.0f,  -120.0f, 0.0f, 0.0f,  120.0f, 0.0f, 0.0f
		};
		float[] PLANE_TEXCOORDS = {
			0.0f, 0.0f,  0.0f, 1.0f,  1.0f, 0.0f,
			1.0f, 0.0f,  0.0f, 1.0f,  1.0f, 1.0f
		};
		float[] PLANE_NORMALS = {
			0.0f, 1.0f, 0.0f,  0.0f, 1.0f, 0.0f,  0.0f, 1.0f, 0.0f,
			0.0f, 1.0f, 0.0f,  0.0f, 1.0f, 0.0f,  0.0f, 1.0f, 0.0f
		};

		// Spheres
		lightSphere = new Sphere(96);
		numLightVerts = lightSphere.getIndices().length;
	
		int[] indices = lightSphere.getIndices();
		Vector3f[] vert = lightSphere.getVertices();
		Vector2f[] tex  = lightSphere.getTexCoords();
		Vector3f[] norm = lightSphere.getNormals();
		
		float[] lpvalues = new float[indices.length*3];
		float[] ltvalues = new float[indices.length*2];
		float[] lnvalues = new float[indices.length*3];
		
		for (int i=0; i<indices.length; i++)
		{	lpvalues[i*3] = (float) (vert[indices[i]]).x;
			lpvalues[i*3+1] = (float) (vert[indices[i]]).y;
			lpvalues[i*3+2] = (float) (vert[indices[i]]).z;
			ltvalues[i*2] = (float) (tex[indices[i]]).x;
			ltvalues[i*2+1] = (float) (tex[indices[i]]).y;
			lnvalues[i*3] = (float) (norm[indices[i]]).x;
			lnvalues[i*3+1]= (float)(norm[indices[i]]).y;
			lnvalues[i*3+2]=(float) (norm[indices[i]]).z;
		}

		float[] cubePositions =
		{	-1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, 1.0f,  1.0f, -1.0f, -1.0f,  1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, 1.0f, -1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
			1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f, 1.0f,  1.0f, -1.0f,
			1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f, -1.0f,  1.0f,  1.0f, 1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f, -1.0f, -1.0f,  1.0f, -1.0f, -1.0f,  1.0f,  1.0f,
			-1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f, -1.0f,
			1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f,  1.0f,
			-1.0f,  1.0f, -1.0f, 1.0f,  1.0f, -1.0f, 1.0f,  1.0f,  1.0f,
			1.0f,  1.0f,  1.0f, -1.0f,  1.0f,  1.0f, -1.0f,  1.0f, -1.0f
		};

		float[] cubeTextureCoordinates = {
			0,1,0,0,1,1, 1,0,1,1,0,0,
			0,0,0,1,1,0, 1,0,1,1,0,1,
			1,0,1,1,0,1, 0,0,1,1,0,1,
			1,0,1,1,0,0, 0,0,1,0,0,1,
			0,1,1,0,1,1, 1,0,0,1,0,0,
			0,0,1,0,1,1, 1,1,0,1,0,0
		};
		
		float[] pyramidPositions =
		{	-1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,    //front
			1.0f, -1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,    //right
			1.0f, -1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 0.0f, 1.0f, 0.0f,  //back
			-1.0f, -1.0f, -1.0f, -1.0f, -1.0f, 1.0f, 0.0f, 1.0f, 0.0f,  //left
			-1.0f, -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, //LF
			1.0f, -1.0f, 1.0f, -1.0f, -1.0f, -1.0f, 1.0f, -1.0f, -1.0f  //RR
		};

		float[] xLinePosition = {
			-100.0f, 0.0f, 0.0f,
			100.0f, 0.0f, 0.0f
		};

		float[] yLinePosition = {
			0.0f, -100.0f, 0.0f,
			0.0f, 100.0f, 0.0f
		};

		float[] zLinePosition = {
			0.0f, 0.0f, -100.0f,
			0.0f, 0.0f, 100.0f
		};

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		FloatBuffer cubeBuf = Buffers.newDirectFloatBuffer(cubePositions);
		gl.glBufferData(GL_ARRAY_BUFFER, cubeBuf.limit()*4, cubeBuf, GL_STATIC_DRAW);

		// Axis's
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[2]);
		FloatBuffer xLine = Buffers.newDirectFloatBuffer(xLinePosition);
		gl.glBufferData(GL_ARRAY_BUFFER, xLine.limit()*4, xLine, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[3]);
		FloatBuffer yLine = Buffers.newDirectFloatBuffer(yLinePosition);
		gl.glBufferData(GL_ARRAY_BUFFER, yLine.limit()*4, yLine, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[4]);
		FloatBuffer zLine = Buffers.newDirectFloatBuffer(zLinePosition);
		gl.glBufferData(GL_ARRAY_BUFFER, zLine.limit()*4, zLine, GL_STATIC_DRAW);


		// Main Dolphin
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[5]); // 0
		FloatBuffer vertBuf = Buffers.newDirectFloatBuffer(pvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, vertBuf.limit()*4, vertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[6]); // 1
		FloatBuffer texBuf = Buffers.newDirectFloatBuffer(tvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, texBuf.limit()*4, texBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[7]); // 2
		FloatBuffer norBuf = Buffers.newDirectFloatBuffer(nvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, norBuf.limit()*4,norBuf, GL_STATIC_DRAW);
		
		// Spheres
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[8]);
		FloatBuffer lightVertBuf = Buffers.newDirectFloatBuffer(lpvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, lightVertBuf.limit()*4, lightVertBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[9]);
		FloatBuffer lightTexBuf = Buffers.newDirectFloatBuffer(ltvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, lightTexBuf.limit()*4, lightTexBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[10]);
		FloatBuffer lightNorBuf = Buffers.newDirectFloatBuffer(lnvalues);
		gl.glBufferData(GL_ARRAY_BUFFER, lightNorBuf.limit()*4, lightNorBuf, GL_STATIC_DRAW);

		// Plane
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[11]);
		FloatBuffer planeBuf = Buffers.newDirectFloatBuffer(PLANE_POSITIONS);
		gl.glBufferData(GL_ARRAY_BUFFER, planeBuf.limit()*4, planeBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[12]);
		FloatBuffer planeTexBuf = Buffers.newDirectFloatBuffer(PLANE_TEXCOORDS);
		gl.glBufferData(GL_ARRAY_BUFFER, planeTexBuf.limit()*4, planeTexBuf, GL_STATIC_DRAW);

		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[13]);
		FloatBuffer planeNorBuf = Buffers.newDirectFloatBuffer(PLANE_NORMALS);
		gl.glBufferData(GL_ARRAY_BUFFER, planeNorBuf.limit()*4, planeNorBuf, GL_STATIC_DRAW);
	}

	private int load3DTexture()
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		byte[] data = new byte[texWidth*texHeight*texDepth*4];
		
		fillDataArray3D(data);

		ByteBuffer bb = Buffers.newDirectByteBuffer(data);

		int[] textureIDs = new int[1];
		gl.glGenTextures(1, textureIDs, 0);
		int textureID = textureIDs[0];

		gl.glBindTexture(GL_TEXTURE_3D, textureID);

		gl.glTexStorage3D(GL_TEXTURE_3D, 1, GL_RGBA8, texWidth, texHeight, texDepth);
		gl.glTexSubImage3D(GL_TEXTURE_3D, 0, 0, 0, 0,
				texWidth, texHeight, texDepth, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, bb);
		
		gl.glTexParameteri(GL_TEXTURE_3D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

		return textureID;
	}

	void generate3Dpattern()
	{	for (int x=0; x<texWidth; x++)
		{	for (int y=0; y<texHeight; y++)
			{	for (int z=0; z<texDepth; z++)
				{	if ((y/10)%2 == 0)
						tex3Dpattern[x][y][z] = 0.0;
					else
						tex3Dpattern[x][y][z] = 1.0;
	}	}	}	}

	public static void main(String[] args) { new Code(); }
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
	public void dispose(GLAutoDrawable drawable) {}


	@Override
	public void keyReleased(KeyEvent e) {
    }

	@Override
	public void keyTyped(KeyEvent e) {
    }

	@Override
	public void keyPressed(KeyEvent e) {

		int key = e.getKeyCode();

		if (key == KeyEvent.VK_W) {
			System.out.println("w");
			//cameraPosition.wMove(timeDiff/2);
			cameraPosition.wMove(0.5f);
			dolphinLocation = new Vector3f(dolphinLocation.x(), dolphinLocation.y(), dolphinLocation.z()-0.5f);
			System.out.println(dolphinLocation);
		}

		if (key == KeyEvent.VK_S) {
			System.out.println("s");
			//cameraPosition.wMove(-timeDiff/2);
			cameraPosition.wMove(-0.5f);
			dolphinLocation = new Vector3f(dolphinLocation.x(), dolphinLocation.y(), dolphinLocation.z()+0.5f);
			System.out.println(dolphinLocation);
		}

		if (key == KeyEvent.VK_A) {
			System.out.println("a");
			//cameraX-=0.1;
			//cameraPosition.dMove(-timeDiff/2);
			cameraPosition.dMove(-0.5f);
			dolphinLocation = new Vector3f(dolphinLocation.x()+0.5f, dolphinLocation.y(), dolphinLocation.z());
			System.out.println(dolphinLocation);
		}

		if (key == KeyEvent.VK_D) {
			System.out.println("d");
			//cameraPosition.dMove(timeDiff/2);
			cameraPosition.dMove(0.5f);
			dolphinLocation = new Vector3f(dolphinLocation.x()-0.5f, dolphinLocation.y(), dolphinLocation.z());
			System.out.println(dolphinLocation);
		}

		if (key == KeyEvent.VK_E) {
			System.out.println("e");
			//cameraPosition.vertMove(-timeDiff/2);
			cameraPosition.vertMove(-0.5f);
			dolphinLocation = new Vector3f(dolphinLocation.x(), dolphinLocation.y()-0.5f, dolphinLocation.z());
			System.out.println(dolphinLocation);
		}

		if (key == KeyEvent.VK_Q) {
			System.out.println("q");
			//cameraPosition.vertMove(timeDiff/2);
			cameraPosition.vertMove(0.5f);
			dolphinLocation = new Vector3f(dolphinLocation.x(), dolphinLocation.y()+0.5f, dolphinLocation.z());
			System.out.println(dolphinLocation);
		}
	
		if (key == KeyEvent.VK_LEFT) {
			System.out.println("left");
			cameraPosition.setPitch(-5.0f);
		}
	
		if (key == KeyEvent.VK_RIGHT) {
			System.out.println("right");
			cameraPosition.setPitch(5.0f);
		}
	
		if (key == KeyEvent.VK_UP) {
			System.out.println("up");
			cameraPosition.setYaw(5.0f);
		}
	
		if (key == KeyEvent.VK_DOWN) {
			System.out.println("down");
			cameraPosition.setYaw(-5.0f);
		}

		if (key == KeyEvent.VK_SPACE) {
			System.out.println("space");
			if (axisBoolean){
				axisBoolean = false;
			}else{
				axisBoolean = true;
			}
		}

		if (key == KeyEvent.VK_U){
			currentLightPos = new Vector3f(currentLightPos.x(),currentLightPos.y(),currentLightPos.z()-1);
		}

		if (key == KeyEvent.VK_J){
			currentLightPos = new Vector3f(currentLightPos.x(),currentLightPos.y(),currentLightPos.z()+1);
		}

		if (key == KeyEvent.VK_H){
			currentLightPos = new Vector3f(currentLightPos.x()-1,currentLightPos.y(),currentLightPos.z());
		}

		if (key == KeyEvent.VK_K){
			currentLightPos = new Vector3f(currentLightPos.x()+1,currentLightPos.y(),currentLightPos.z());
		}

		if (key == KeyEvent.VK_Y){
			currentLightPos = new Vector3f(currentLightPos.x(),currentLightPos.y()+1,currentLightPos.z());
			System.out.println(currentLightPos);
		}

		if (key == KeyEvent.VK_I){
			currentLightPos = new Vector3f(currentLightPos.x(),currentLightPos.y()-1,currentLightPos.z());
		}
	}
}