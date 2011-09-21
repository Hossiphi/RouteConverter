//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-646 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.08.13 at 10:12:26 PM MESZ 
//


package slash.navigation.kml.binding22;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for LatLonAltBoxType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LatLonAltBoxType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/kml/2.2}AbstractLatLonBoxType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}minAltitude" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}maxAltitude" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}altitudeModeGroup" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}LatLonAltBoxSimpleExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/kml/2.2}LatLonAltBoxObjectExtensionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LatLonAltBoxType", propOrder = {
    "minAltitude",
    "maxAltitude",
    "altitudeModeGroup",
    "latLonAltBoxSimpleExtensionGroup",
    "latLonAltBoxObjectExtensionGroup"
})
public class LatLonAltBoxType
    extends AbstractLatLonBoxType
{

    @XmlElement(defaultValue = "0.0")
    protected Double minAltitude;
    @XmlElement(defaultValue = "0.0")
    protected Double maxAltitude;
    @XmlElementRef(name = "altitudeModeGroup", namespace = "http://www.opengis.net/kml/2.2", type = JAXBElement.class)
    protected JAXBElement<?> altitudeModeGroup;
    @XmlElement(name = "LatLonAltBoxSimpleExtensionGroup")
    @XmlSchemaType(name = "anySimpleType")
    protected List<Object> latLonAltBoxSimpleExtensionGroup;
    @XmlElement(name = "LatLonAltBoxObjectExtensionGroup")
    protected List<AbstractObjectType> latLonAltBoxObjectExtensionGroup;

    /**
     * Gets the value of the minAltitude property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getMinAltitude() {
        return minAltitude;
    }

    /**
     * Sets the value of the minAltitude property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setMinAltitude(Double value) {
        this.minAltitude = value;
    }

    /**
     * Gets the value of the maxAltitude property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getMaxAltitude() {
        return maxAltitude;
    }

    /**
     * Sets the value of the maxAltitude property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setMaxAltitude(Double value) {
        this.maxAltitude = value;
    }

    /**
     * Gets the value of the altitudeModeGroup property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link slash.navigation.kml.binding22.AltitudeModeEnumType }{@code >}
     *     {@link JAXBElement }{@code <}{@link slash.navigation.kml.binding22gx.AltitudeModeEnumType }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     
     */
    public JAXBElement<?> getAltitudeModeGroup() {
        return altitudeModeGroup;
    }

    /**
     * Sets the value of the altitudeModeGroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link slash.navigation.kml.binding22.AltitudeModeEnumType }{@code >}
     *     {@link JAXBElement }{@code <}{@link slash.navigation.kml.binding22gx.AltitudeModeEnumType }{@code >}
     *     {@link JAXBElement }{@code <}{@link Object }{@code >}
     *     
     */
    public void setAltitudeModeGroup(JAXBElement<?> value) {
        this.altitudeModeGroup = ((JAXBElement<?> ) value);
    }

    /**
     * Gets the value of the latLonAltBoxSimpleExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the latLonAltBoxSimpleExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLatLonAltBoxSimpleExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getLatLonAltBoxSimpleExtensionGroup() {
        if (latLonAltBoxSimpleExtensionGroup == null) {
            latLonAltBoxSimpleExtensionGroup = new ArrayList<Object>();
        }
        return this.latLonAltBoxSimpleExtensionGroup;
    }

    /**
     * Gets the value of the latLonAltBoxObjectExtensionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the latLonAltBoxObjectExtensionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLatLonAltBoxObjectExtensionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AbstractObjectType }
     * 
     * 
     */
    public List<AbstractObjectType> getLatLonAltBoxObjectExtensionGroup() {
        if (latLonAltBoxObjectExtensionGroup == null) {
            latLonAltBoxObjectExtensionGroup = new ArrayList<AbstractObjectType>();
        }
        return this.latLonAltBoxObjectExtensionGroup;
    }

}
