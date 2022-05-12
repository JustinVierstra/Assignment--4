package a4;
import org.joml.*;
import java.lang.Math;

public class Camera {

    private Vector3f u, v, n;
	private Vector3f oldu, oldv, oldn;
	private Vector3f newu, newv, newn;
	private Vector3f defaultU, defaultV, defaultN;
	private Vector3f location, defaultLocation;
	private Vector3f rightVector, upVector, fwdVector, oldPosition, newPosition;
	private Vector3f cameraFront;
	private Matrix4f view, viewR, viewT;
	private float yaw, pitch, roll;

	/** instantiates a Camera object at location (0,0,1) and facing down the -Z axis towards the origin */
	public Camera(float cameraX, float cameraY, float cameraZ){	
		defaultLocation = new Vector3f(cameraX, cameraY, cameraZ);
		defaultU = new Vector3f(1.0f, 0.0f, 0.0f);
		defaultV = new Vector3f(0.0f, 1.0f, 0.0f);
		defaultN = new Vector3f(0.0f, 0.0f, -1.0f);
		location = new Vector3f(defaultLocation);
		u = new Vector3f(defaultU);
		v = new Vector3f(defaultV);
		n = new Vector3f(defaultN);
		view = new Matrix4f();
		viewR = new Matrix4f();
		viewT = new Matrix4f();
		//pitch = 15.0f
	}

	public Vector3f wMove(float x){
		oldPosition = location;
		newPosition = oldPosition.add(-x*n.x(), -x*n.y(), -x*n.z());
		location = newPosition;
		return location;
	}

	public Vector3f dMove(float x){
		oldPosition = location;
		newPosition = oldPosition.add(-x*u.x(), -x*u.y(), -x*u.z());
		location = newPosition;
		return location;
	}

	public Vector3f vertMove(float y){
		oldPosition = location;
		newPosition = oldPosition.add(-y*v.x(), -y*v.y(), -y*v.z());
		location = newPosition;
		return location;
	}

	public void setLocation(float x, float y, float z){
		location = new Vector3f(x,y,z);
	}

	/** sets the world location of this Camera */
	public void setLocation(Vector3f l) { location.set(l); }

	public Vector3f getLocation(){
		return new Vector3f(location);
	}

	protected Matrix4f getViewMatrix()
	{	viewT.set(1.0f, 0.0f, 0.0f, 0.0f,
		0.0f, 1.0f, 0.0f, 0.0f,
		0.0f, 0.0f, 1.0f, 0.0f,
		-location.x(), -location.y(), -location.z(), 1.0f);

		viewR.set(u.x(), v.x(), -n.x(), 0.0f,
		u.y(), v.y(), -n.y(), 0.0f,
		u.z(), v.z(), -n.z(), 0.0f,
		0.0f, 0.0f, 0.0f, 1.0f);

		view.identity();
		view.mul(viewR);
		view.mul(viewT);

		return(view);
	}

	public void setYaw(float x){
		yaw += x;
	}

	public float getYaw(){
		return yaw;
	}

	public void setPitch(float y){
		pitch += y;
	}

	public float getPitch(){
		return pitch;
	}

	public void setRoll(float z){
		roll += z;
	}

	public float getRoll(){
		return roll;
	}

	public float getHeight(){
		return location.y()*-1;
	}

	public float getX(){
		return location.x()*-1;
	}

	public float getY(){
		return location.y()*-1;
	}

	public float getZ(){
		return location.z()*-1;
	}

	public void forward(float y){
		defaultLocation = new Vector3f(defaultLocation.x(), defaultLocation.y()+y, defaultLocation.z());
	}
}
