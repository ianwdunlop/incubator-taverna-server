/*
 */
package org.taverna.server.port_description;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "LeafValue")
public class LeafValue extends AbstractValue {
	@XmlAttribute(name = "contentFile")
	public String fileName;
	@XmlAttribute(name = "contentType")
	public String contentType;
	@XmlAttribute(name = "contentByteLength")
	public Long byteLength;
}
