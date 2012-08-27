package info.novatec.inspectit.storage.nio;

import info.novatec.inspectit.spring.logger.Logger;
import info.novatec.inspectit.storage.nio.bytebuffer.ByteBufferPoolFactory;

import java.nio.ByteBuffer;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This component enables reusing of the {@link ByteBuffer}s. It creates a set of initial buffers
 * that are available for "rent". The amount of buffers created at the beginning is going to be
 * enough to satisfies {@link #poolMinCapacity}. The total amount of buffers created will never go
 * above {@link #poolMaxCapacity}.
 * <P>
 * This class does not have protection against not returning a buffer, thus all components using the
 * provider have to ensure they will return the buffer to the provider.
 * 
 * @author Ivan Senic
 * 
 */
@Component
public class ByteBufferProvider extends GenericObjectPool<ByteBuffer> {

	/**
	 * The log of this class.
	 */
	@Logger
	Log log;

	/**
	 * Default buffer size that will be used if not initial buffer list size is provided to create
	 * one buffer.
	 */
	public static final int DEFAULT_BUFFER_CAPACITY = 1024 * 1024;

	/**
	 * Buffer size.
	 */
	@Value(value = "${storage.bufferSize}")
	private int bufferSize = DEFAULT_BUFFER_CAPACITY;

	/**
	 * Pool factory.
	 */
	private ByteBufferPoolFactory poolFactory;

	/**
	 * Amount of bytes this pool can acquire in total.
	 * <p>
	 * <i>Hard coded to 150MB</i>
	 */
	@Value(value = "${storage.bufferPoolMaxCapacity}")
	private long poolMaxCapacity;

	/**
	 * Amount of bytes this pool should have at minimum.
	 * <p>
	 * <i>Hard coded to 30MB</i>
	 */
	@Value(value = "${storage.bufferPoolMinCapacity}")
	private long poolMinCapacity;

	/**
	 * Default constructor.
	 */
	public ByteBufferProvider() {
		this(new ByteBufferPoolFactory(DEFAULT_BUFFER_CAPACITY));
	}

	/**
	 * @param poolFactory
	 *            Pool factory to be used.
	 */
	protected ByteBufferProvider(ByteBufferPoolFactory poolFactory) {
		super(poolFactory);
		this.poolFactory = poolFactory;
	}

	/**
	 * Returns buffer from the pool. This method waits for buffer to be available or creates a new
	 * buffer if the {@link #createdCapacity} is less than {@link #poolMaxCapacity}.
	 * 
	 * @return {@link ByteBuffer}.
	 */
	public ByteBuffer acquireByteBuffer() {
		try {
			return super.borrowObject();
		} catch (Exception e) {
			log.error("Byte buffer pool can not borrow a valid byte buffer.", e);
			return null;
		}
	}

	/**
	 * Gives back the {@link ByteBuffer} to the pool, so that others can use it.
	 * 
	 * @param byteBuffer
	 *            {@link ByteBuffer} to put back to the pool.
	 * @throws InterruptedException
	 */
	public void releaseByteBuffer(ByteBuffer byteBuffer) {
		try {
			super.returnObject(byteBuffer);
		} catch (Exception e) {
			log.error("Byte buffer can not be returned to the byte buffer pool.", e);
			return;
		}
	}

	/**
	 * @return Returns the pool size.
	 */
	int getBufferPoolSize() {
		return super.getNumIdle();
	}

	/**
	 * Gets {@link #createdCapacity}.
	 * 
	 * @return {@link #createdCapacity}
	 */
	long getCreatedCapacity() {
		return (super.getNumActive() + super.getNumIdle()) * bufferSize;
	}

	/**
	 * Gets {@link #availableCapacity}.
	 * 
	 * @return {@link #availableCapacity}
	 */
	long getAvailableCapacity() {
		return super.getNumIdle() * bufferSize;
	}

	/**
	 * <i>This setter can be removed when the Spring3.0 on the GUI side is working properly.</i>
	 * 
	 * @param poolMaxCapacity
	 *            the poolMaxCapacity to set
	 */
	public void setPoolMaxCapacity(long poolMaxCapacity) {
		this.poolMaxCapacity = poolMaxCapacity;
	}

	/**
	 * Sets {@link #poolMinCapacity}.
	 * 
	 * @param poolMinCapacity
	 *            New value for {@link #poolMinCapacity}
	 */
	public void setPoolMinCapacity(long poolMinCapacity) {
		this.poolMinCapacity = poolMinCapacity;
	}

	/**
	 * Sets {@link #bufferSize}.
	 * 
	 * @param bufferSize
	 *            New value for {@link #bufferSize}
	 */
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	/**
	 * Initializes the pool.
	 */
	@PostConstruct
	protected void init() {
		poolFactory.setBufferCpacity(bufferSize);
		int maxIdle = (int) (poolMinCapacity / bufferSize);
		int maxActive = (int) (poolMaxCapacity / bufferSize);
		super.setMaxIdle(maxIdle);
		super.setMaxActive(maxActive);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		ToStringBuilder toStringBuilder = new ToStringBuilder(this);
		toStringBuilder.append("poolMinCapacity", poolMinCapacity);
		toStringBuilder.append("poolMaxCapacity", poolMaxCapacity);
		return toStringBuilder.toString();
	}

}
