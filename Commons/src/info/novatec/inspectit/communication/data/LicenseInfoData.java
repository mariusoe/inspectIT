package info.novatec.inspectit.communication.data;

import java.io.Serializable;
import java.util.Date;

/**
 * POJO for transfering information about license between CMR and UI.
 * 
 * @author Ivan Senic
 * 
 */
public class LicenseInfoData implements Serializable {

	/**
	 * Generated UID.
	 */
	private static final long serialVersionUID = 8955524336885955917L;

	/**
	 * Holder of license.
	 */
	private String holder;

	/**
	 * Issuer of license.
	 */
	private String issuer;

	/**
	 * Date issued.
	 */
	private Date issued;

	/**
	 * Not valid before date.
	 */
	private Date notBefore;

	/**
	 * Not valid after date.
	 */
	private Date notAfter;

	/**
	 * Subbject.
	 */
	private String subject;

	/**
	 * Consumer type.
	 */
	private String consumerType;

	/**
	 * Consumer amount.
	 */
	private int consumerAmount;

	/**
	 * Additional info.
	 */
	private String info;

	/**
	 * Maximum connected agents allowed.
	 */
	private int maximumAgents;

	public String getHolder() {
		return holder;
	}

	public void setHolder(String holder) {
		this.holder = holder;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public Date getIssued() {
		return issued;
	}

	public void setIssued(Date issued) {
		this.issued = issued;
	}

	public Date getNotBefore() {
		return notBefore;
	}

	public void setNotBefore(Date notBefore) {
		this.notBefore = notBefore;
	}

	public Date getNotAfter() {
		return notAfter;
	}

	public void setNotAfter(Date notAfter) {
		this.notAfter = notAfter;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getConsumerType() {
		return consumerType;
	}

	public void setConsumerType(String consumerType) {
		this.consumerType = consumerType;
	}

	public int getConsumerAmount() {
		return consumerAmount;
	}

	public void setConsumerAmount(int consumerAmount) {
		this.consumerAmount = consumerAmount;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public int getMaximumAgents() {
		return maximumAgents;
	}

	public void setMaximumAgents(int maximumAgents) {
		this.maximumAgents = maximumAgents;
	}

}
