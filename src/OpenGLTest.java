import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.PMVMatrix;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_SEVERITY_HIGH;
import static com.jogamp.opengl.GL2ES2.GL_DEBUG_SEVERITY_MEDIUM;
import static com.jogamp.opengl.GL2ES3.*;

/**
 * @author Quinn Kleinfelter
 *
 * Changed the vertices and colors arrays to display a pyramid with a 1 unit square base
 * with top point located at (0, 1, 0).
 * Also added in code from Dr. Heuring's Add Rotation example to be able to examine
 * the entire pyramid, as well as added code to accomodate for up and down rotation.
 * Note: Requires new (add rotation example) shader files to appropriately turn on arrow key
 * presses (or WASD key presses).
 */

/**
 * @author Jerry Heuring
 *
 *Wrapped the entire class in OpenGLTest.  This made it easier to work with in 
 *my environment.  I don't like having more than one class in a file and may 
 *try to split this.
 */
public class OpenGLTest {
    private interface Buffer {

        int VERTEX = 0;
        int ELEMENT = 1;
        int GLOBAL_MATRICES = 2;
        int MODEL_MATRIX = 3;
        int MAX = 4;
    }	
    /**
	 * Created by GBarbieri on 16.03.2017.
	 * 
	 * Program heavily modified by Jerry Heuring in September 2021.  
	 * Most modifications stripped out code that was not yet needed
	 * reorganized the remaining code to more closely align with the C/C++
	 * version of the initial program. 
	 */
	public class HelloTriangleSimple implements GLEventListener, KeyListener {

	    private GLWindow window;
	    private Animator animator;

	    public void main(String[] args) {
	        new HelloTriangleSimple().setup();
	    }

		private final float[] vertices = {
				-0.5f, 0.0f, -0.5f, 1.0f, 0.5f, 0.0f, -0.5f, 1.0f, -0.5f, 0.0f, 0.5f, 1.0f, // base triangle 1
				0.5f, 0.0f, 0.5f, 1.0f, 0.5f, 0.0f, -0.5f, 1.0f, -0.5f, 0.0f, 0.5f, 1.0f,   // base triangle 2
				-0.5f, 0.0f, -0.5f, 1.0f, -0.5f, 0.0f, 0.5f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f,  // left side triangle
				-0.5f, 0.0f, 0.5f, 1.0f, 0.5f, 0.0f, 0.5f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f,    // back side triangle
				0.5f, 0.0f, 0.5f, 1.0f, 0.5f, 0.0f, -0.5f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f,	// right side triangle
				-0.5f, 0.0f, -0.5f, 1.0f, 0.5f, 0.0f, -0.5f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f   // front side triangle
		};

		private final float[] colors = {
				1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,		// base triangle 1
				0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,		// base triangle 2
				1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f,		// left side triangle
				1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f,		// back side triangle
				0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f,		// right side triangle
				1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f,		// front side triangle
		};


	    private final IntBuffer bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX);
	    private final IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(1);
        private Program program;
		private final PMVMatrix rotation = new PMVMatrix();

	    private void setup() {

	        GLProfile glProfile = GLProfile.get(GLProfile.GL4);
	        GLCapabilities glCapabilities = new GLCapabilities(glProfile);

	        window = GLWindow.create(glCapabilities);

	        window.setTitle("Hello Triangle (enhanced)");
	        window.setSize(600, 600);

	        window.setContextCreationFlags(GLContext.CTX_OPTION_DEBUG);
	        window.setVisible(true);

	        window.addGLEventListener(this);
	        window.addKeyListener(this);

	        animator = new Animator(window);
	        animator.start();

	        window.addWindowListener(new WindowAdapter() {
	            @Override
	            public void windowDestroyed(WindowEvent e) {
	                animator.stop();
	                System.exit(1);
	            }
	        });
	    }


	    @Override
	    public void init(GLAutoDrawable drawable) {

	        GL4 gl = drawable.getGL().getGL4();

	        initDebug(gl);
	        program = new Program(gl, "src/", "passthrough", "passthrough");
	        rotation.glLoadIdentity();
	        buildObjects(gl);

	        gl.glEnable(GL_DEPTH_TEST);
	    }

	    private void initDebug(GL4 gl) {

	        window.getContext().addGLDebugListener(System.out::println);
	        /*
	         * sets up medium and high severity error messages to be printed.
	         */
	        gl.glDebugMessageControl(
	                GL_DONT_CARE,  GL_DONT_CARE, GL_DONT_CARE,
	                0, null, false);

	        gl.glDebugMessageControl(
	                GL_DONT_CARE,  GL_DONT_CARE, GL_DEBUG_SEVERITY_HIGH,
	                0, null, true);

	        gl.glDebugMessageControl(
	                GL_DONT_CARE,  GL_DONT_CARE, GL_DEBUG_SEVERITY_MEDIUM,
	                0, null, true);
	    }

	    private void buildObjects(GL4 gl) {

	        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(vertices);
	        FloatBuffer colorBuffer  = GLBuffers.newDirectFloatBuffer(colors);

	        gl.glGenVertexArrays(1, vertexArrayName);
	        gl.glBindVertexArray(vertexArrayName.get(0));
	        gl.glGenBuffers(Buffer.MAX, bufferName);
	        gl.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(0));
	        gl.glBufferData(GL_ARRAY_BUFFER,  (vertexBuffer.capacity()+colorBuffer.capacity()) * 4L, null, GL_STATIC_DRAW);
	        gl.glBufferSubData(GL_ARRAY_BUFFER,0L,vertexBuffer.capacity() * 4L, vertexBuffer);
	        gl.glBufferSubData(GL_ARRAY_BUFFER, vertexBuffer.capacity() * 4L, colorBuffer.capacity()* 4L, colorBuffer);

	        int vPosition = gl.glGetAttribLocation(program.name, "vPosition");
	        int vColor = gl.glGetAttribLocation(program.name, "vColor");
	        gl.glEnableVertexAttribArray(vPosition);
	        gl.glVertexAttribPointer(vPosition, 4, GL_FLOAT, false, 0, 0);
	        gl.glEnableVertexAttribArray(vColor);
	        gl.glVertexAttribPointer(vColor, 4, GL_FLOAT, false, 0, vertexBuffer.capacity() * 4L);
	    }


	    @Override
	    /*
	     * Display the object.  One issue with this is that it has the number 
	     * of triangles hardcoded at the moment -- hopefully I will fix this
	     * so that it comes from the buffer or some other reasonable object.
	     * (non-Javadoc)
	     * @see com.jogamp.opengl.GLEventListener#display(com.jogamp.opengl.GLAutoDrawable)
	     */
	    public void display(GLAutoDrawable drawable) {

	        GL4 gl = drawable.getGL().getGL4();

	        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	        gl.glUseProgram(program.name);
	        gl.glBindVertexArray(vertexArrayName.get(0));
	        gl.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(0));
	        int modelMatrixLocation = gl.glGetUniformLocation(program.name, "modelingMatrix");
	        gl.glUniformMatrix4fv(modelMatrixLocation, 1, false, rotation.glGetMatrixf());
	        gl.glDrawArrays(GL_TRIANGLES, 0, vertices.length / 4);
	    }

	    @Override
	    /*
	     * handles window reshapes -- it should affect the size of the
	     * view as well so that things remain square but since we haven't
	     * gotten to projections yet it does not.
	     * @see com.jogamp.opengl.GLEventListener#reshape(com.jogamp.opengl.GLAutoDrawable, int, int, int, int)
	     */
	    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

	        GL4 gl = drawable.getGL().getGL4();
	        gl.glViewport(x, y, width, height);
	    }

	    @Override
/*
 * This method disposes of resources cleaning up
 * at the end.  This wasn't happening in the C/C++
 * version but would be a good idea.
 */ 
  	    public void dispose(GLAutoDrawable drawable) {
	        GL4 gl = drawable.getGL().getGL4();

	        gl.glDeleteProgram(program.name);
	        gl.glDeleteVertexArrays(1, vertexArrayName);
	        gl.glDeleteBuffers(Buffer.MAX, bufferName);
	    }

	    @Override
	    /*
	     * Keypress callback for java -- handle a keypress
	     * (non-Javadoc)
	     * @see com.jogamp.newt.event.KeyListener#keyPressed(com.jogamp.newt.event.KeyEvent)
	     */
	    public void keyPressed(KeyEvent e) {
	        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
	            new Thread(() -> {
	                window.destroy();
	            }).start();
	        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
	        	rotation.glRotatef(10.0f, 0.0f, 1.0f, 0.0f);
	        } else if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
	        	rotation.glRotatef(-10.0f, 0.0f, 1.0f, 0.0f);
	        } else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
				rotation.glRotatef(10.0f, 1.0f, 0.0f, 0.0f);
			} else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
				rotation.glRotatef(-10.0f, 1.0f, 0.0f, 0.0f);
			}
	    }

	    @Override
	    public void keyReleased(KeyEvent e) {
	    }

	    	/*
	    	 * private class to handle building the shader
	    	 * program from filenames.  This one is different
	    	 * from the C/C++ one in that it does not take the
	    	 * complete path.  It has a path and a file name and
	    	 * then insists on the extensions .vert, .frag.
	    	 * 
	    	 * I think we will rewrite this one to do a few other
	    	 * things before the class is over.  Right now it works.
	    	 */
	    private class Program {

	        public int name = 0;

	        public Program(GL4 gl, String root, String vertex, String fragment) {

	            ShaderCode vertShader = ShaderCode.create(gl, GL_VERTEX_SHADER, this.getClass(), root, null, vertex,
	                    "vert", null, true);
	            ShaderCode fragShader = ShaderCode.create(gl, GL_FRAGMENT_SHADER, this.getClass(), root, null, fragment,
	                    "frag", null, true);

	            ShaderProgram shaderProgram = new ShaderProgram();

	            shaderProgram.add(vertShader);
	            shaderProgram.add(fragShader);

	            shaderProgram.init(gl);

	            name = shaderProgram.program();

	            shaderProgram.link(gl, System.err);
	        }
	    }

	    /*
	     * Class to set up debug output from OpenGL.
	     * Again, I haven't done this in the C/C++ version
	     * but it would be a good idea.  
	     */
	    private class GlDebugOutput implements GLDebugListener {

	        private int source = 0;
	        private int type = 0;
	        private int id = 0;
	        private int severity = 0;
			private String message = null;

			public GlDebugOutput() {
	        }

	        public GlDebugOutput(int source, int type, int severity) {
	            this.source = source;
	            this.type = type;
	            this.severity = severity;
	            this.message = null;
	            this.id = -1;
	        }

	        public GlDebugOutput(String message, int id) {
	            this.source = -1;
	            this.type = -1;
	            this.severity = -1;
	            this.message = message;
	            this.id = id;
	        }

	        @Override
	        public void messageSent(GLDebugMessage event) {

	            if (event.getDbgSeverity() == GL_DEBUG_SEVERITY_LOW || event.getDbgSeverity() == GL_DEBUG_SEVERITY_NOTIFICATION)
	                System.out.println("GlDebugOutput.messageSent(): " + event);
	            else
	                System.err.println("GlDebugOutput.messageSent(): " + event);

				boolean received = false;
				if (null != message && message.equals(event.getDbgMsg()) && id == event.getDbgId())
	                received = true;
	            else if (0 <= source && source == event.getDbgSource() && type == event.getDbgType() && severity == event.getDbgSeverity())
	                received = true;
	        }
	    }
	}
	/**
	 * Default constructor for the class does nothing in this case.
	 * It simply gives a starting point to create an instance and then 
	 * run the main program from the class.
	 */
	public OpenGLTest() {
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		OpenGLTest myInstance = new OpenGLTest();
		HelloTriangleSimple example = myInstance.new HelloTriangleSimple();
		example.main(args);
	}

}
