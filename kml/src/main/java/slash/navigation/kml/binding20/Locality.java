//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.02.17 at 01:40:15 PM MEZ
//


package slash.navigation.kml.binding20;

import javax.xml.bind.annotation.*;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}AddressLine" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="LocalityName" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attGroup ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}grPostal"/>
 *                 &lt;attribute name="Type" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;choice minOccurs="0">
 *           &lt;element ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}PostBox"/>
 *           &lt;element name="LargeMailUser" type="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}LargeMailUserType"/>
 *           &lt;element ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}PostOffice"/>
 *           &lt;element name="PostalRoute" type="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}PostalRouteType"/>
 *         &lt;/choice>
 *         &lt;element ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}Thoroughfare" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}Premise" minOccurs="0"/>
 *         &lt;element name="DependentLocality" type="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}DependentLocalityType" minOccurs="0"/>
 *         &lt;element ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}PostalCode" minOccurs="0"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attribute name="Indicator" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="Type" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *       &lt;attribute name="UsageType" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "addressLine",
    "localityName",
    "postBox",
    "largeMailUser",
    "postOffice",
    "postalRoute",
    "thoroughfare",
    "premise",
    "dependentLocality",
    "postalCode",
    "any"
})
@XmlRootElement(name = "Locality", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0")
public class Locality {

    @XmlElement(name = "AddressLine", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0")
    protected List<AddressLine> addressLine;
    @XmlElement(name = "LocalityName", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0")
    protected List<Locality.LocalityName> localityName;
    @XmlElement(name = "PostBox", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0")
    protected PostBox postBox;
    @XmlElement(name = "LargeMailUser", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0")
    protected LargeMailUserType largeMailUser;
    @XmlElement(name = "PostOffice", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0")
    protected PostOffice postOffice;
    @XmlElement(name = "PostalRoute", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0")
    protected PostalRouteType postalRoute;
    @XmlElement(name = "Thoroughfare", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0")
    protected Thoroughfare thoroughfare;
    @XmlElement(name = "Premise", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0")
    protected Premise premise;
    @XmlElement(name = "DependentLocality", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0")
    protected DependentLocalityType dependentLocality;
    @XmlElement(name = "PostalCode", namespace = "urn:oasis:names:tc:ciq:xsdschema:xAL:2.0")
    protected PostalCode postalCode;
    @XmlAnyElement(lax = true)
    protected List<Object> any;
    @XmlAttribute(name = "Indicator")
    protected String indicator;
    @XmlAttribute(name = "Type")
    protected String type;
    @XmlAttribute(name = "UsageType")
    protected String usageType;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the addressLine property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the addressLine property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAddressLine().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AddressLine }
     * 
     * 
     */
    public List<AddressLine> getAddressLine() {
        if (addressLine == null) {
            addressLine = new ArrayList<AddressLine>();
        }
        return this.addressLine;
    }

    /**
     * Gets the value of the localityName property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the localityName property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLocalityName().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Locality.LocalityName }
     * 
     * 
     */
    public List<Locality.LocalityName> getLocalityName() {
        if (localityName == null) {
            localityName = new ArrayList<Locality.LocalityName>();
        }
        return this.localityName;
    }

    /**
     * Gets the value of the postBox property.
     * 
     * @return
     *     possible object is
     *     {@link PostBox }
     *     
     */
    public PostBox getPostBox() {
        return postBox;
    }

    /**
     * Sets the value of the postBox property.
     * 
     * @param value
     *     allowed object is
     *     {@link PostBox }
     *     
     */
    public void setPostBox(PostBox value) {
        this.postBox = value;
    }

    /**
     * Gets the value of the largeMailUser property.
     * 
     * @return
     *     possible object is
     *     {@link LargeMailUserType }
     *     
     */
    public LargeMailUserType getLargeMailUser() {
        return largeMailUser;
    }

    /**
     * Sets the value of the largeMailUser property.
     * 
     * @param value
     *     allowed object is
     *     {@link LargeMailUserType }
     *     
     */
    public void setLargeMailUser(LargeMailUserType value) {
        this.largeMailUser = value;
    }

    /**
     * Gets the value of the postOffice property.
     * 
     * @return
     *     possible object is
     *     {@link PostOffice }
     *     
     */
    public PostOffice getPostOffice() {
        return postOffice;
    }

    /**
     * Sets the value of the postOffice property.
     * 
     * @param value
     *     allowed object is
     *     {@link PostOffice }
     *     
     */
    public void setPostOffice(PostOffice value) {
        this.postOffice = value;
    }

    /**
     * Gets the value of the postalRoute property.
     * 
     * @return
     *     possible object is
     *     {@link PostalRouteType }
     *     
     */
    public PostalRouteType getPostalRoute() {
        return postalRoute;
    }

    /**
     * Sets the value of the postalRoute property.
     * 
     * @param value
     *     allowed object is
     *     {@link PostalRouteType }
     *     
     */
    public void setPostalRoute(PostalRouteType value) {
        this.postalRoute = value;
    }

    /**
     * Gets the value of the thoroughfare property.
     * 
     * @return
     *     possible object is
     *     {@link Thoroughfare }
     *     
     */
    public Thoroughfare getThoroughfare() {
        return thoroughfare;
    }

    /**
     * Sets the value of the thoroughfare property.
     * 
     * @param value
     *     allowed object is
     *     {@link Thoroughfare }
     *     
     */
    public void setThoroughfare(Thoroughfare value) {
        this.thoroughfare = value;
    }

    /**
     * Gets the value of the premise property.
     * 
     * @return
     *     possible object is
     *     {@link Premise }
     *     
     */
    public Premise getPremise() {
        return premise;
    }

    /**
     * Sets the value of the premise property.
     * 
     * @param value
     *     allowed object is
     *     {@link Premise }
     *     
     */
    public void setPremise(Premise value) {
        this.premise = value;
    }

    /**
     * Gets the value of the dependentLocality property.
     * 
     * @return
     *     possible object is
     *     {@link DependentLocalityType }
     *     
     */
    public DependentLocalityType getDependentLocality() {
        return dependentLocality;
    }

    /**
     * Sets the value of the dependentLocality property.
     * 
     * @param value
     *     allowed object is
     *     {@link DependentLocalityType }
     *     
     */
    public void setDependentLocality(DependentLocalityType value) {
        this.dependentLocality = value;
    }

    /**
     * Gets the value of the postalCode property.
     * 
     * @return
     *     possible object is
     *     {@link PostalCode }
     *     
     */
    public PostalCode getPostalCode() {
        return postalCode;
    }

    /**
     * Sets the value of the postalCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link PostalCode }
     *     
     */
    public void setPostalCode(PostalCode value) {
        this.postalCode = value;
    }

    /**
     * Gets the value of the any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Object }
     * 
     * 
     */
    public List<Object> getAny() {
        if (any == null) {
            any = new ArrayList<Object>();
        }
        return this.any;
    }

    /**
     * Gets the value of the indicator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIndicator() {
        return indicator;
    }

    /**
     * Sets the value of the indicator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIndicator(String value) {
        this.indicator = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the usageType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsageType() {
        return usageType;
    }

    /**
     * Sets the value of the usageType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsageType(String value) {
        this.usageType = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attGroup ref="{urn:oasis:names:tc:ciq:xsdschema:xAL:2.0}grPostal"/>
     *       &lt;attribute name="Type" type="{http://www.w3.org/2001/XMLSchema}anySimpleType" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "content"
    })
    public static class LocalityName {

        @XmlValue
        protected String content;
        @XmlAttribute(name = "Type")
        protected String type;
        @XmlAttribute(name = "Code")
        protected String code;
        @XmlAnyAttribute
        private Map<QName, String> otherAttributes = new HashMap<QName, String>();

        /**
         * Gets the value of the content property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getContent() {
            return content;
        }

        /**
         * Sets the value of the content property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setContent(String value) {
            this.content = value;
        }

        /**
         * Gets the value of the type property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the value of the type property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setType(String value) {
            this.type = value;
        }

        /**
         * Gets the value of the code property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCode() {
            return code;
        }

        /**
         * Sets the value of the code property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCode(String value) {
            this.code = value;
        }

        /**
         * Gets a map that contains attributes that aren't bound to any typed property on this class.
         * 
         * <p>
         * the map is keyed by the name of the attribute and 
         * the value is the string value of the attribute.
         * 
         * the map returned by this method is live, and you can add new attribute
         * by updating the map directly. Because of this design, there's no setter.
         * 
         * 
         * @return
         *     always non-null
         */
        public Map<QName, String> getOtherAttributes() {
            return otherAttributes;
        }

    }

}
