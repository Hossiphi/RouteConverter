//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.02.17 at 01:40:15 PM MEZ
//


package slash.navigation.kml.binding20;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded" minOccurs="0">
 *         &lt;element ref="{http://earth.google.com/kml/2.0}name" minOccurs="0"/>
 *         &lt;element ref="{http://earth.google.com/kml/2.0}Status"/>
 *         &lt;element ref="{http://earth.google.com/kml/2.0}Placemark" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "nameOrStatusOrPlacemark"
})
@XmlRootElement(name = "Response")
public class Response {

    @XmlElements({
        @XmlElement(name = "name", type = String.class),
        @XmlElement(name = "Placemark", type = Placemark.class),
        @XmlElement(name = "Status", type = Status.class)
    })
    protected List<Object> nameOrStatusOrPlacemark;

    /**
     * Gets the value of the nameOrStatusOrPlacemark property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the nameOrStatusOrPlacemark property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNameOrStatusOrPlacemark().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * {@link Placemark }
     * {@link Status }
     * 
     * 
     */
    public List<Object> getNameOrStatusOrPlacemark() {
        if (nameOrStatusOrPlacemark == null) {
            nameOrStatusOrPlacemark = new ArrayList<Object>();
        }
        return this.nameOrStatusOrPlacemark;
    }

}
