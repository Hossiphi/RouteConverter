//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2009.02.17 at 01:40:15 PM MEZ
//


package slash.navigation.kml.binding20;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element ref="{http://earth.google.com/kml/2.0}name" minOccurs="0"/>
 *         &lt;element ref="{http://earth.google.com/kml/2.0}ObjArrayField" minOccurs="0"/>
 *         &lt;element ref="{http://earth.google.com/kml/2.0}ObjField" minOccurs="0"/>
 *         &lt;element ref="{http://earth.google.com/kml/2.0}SimpleArrayField" minOccurs="0"/>
 *         &lt;element ref="{http://earth.google.com/kml/2.0}SimpleField" minOccurs="0"/>
 *         &lt;element ref="{http://earth.google.com/kml/2.0}parent" minOccurs="0"/>
 *       &lt;/all>
 *       &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}ID" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {

})
@XmlRootElement(name = "Schema")
public class Schema {

    protected String name;
    @XmlElement(name = "ObjArrayField")
    protected ObjArrayField objArrayField;
    @XmlElement(name = "ObjField")
    protected ObjField objField;
    @XmlElement(name = "SimpleArrayField")
    protected SimpleArrayField simpleArrayField;
    @XmlElement(name = "SimpleField")
    protected SimpleField simpleField;
    protected String parent;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the objArrayField property.
     * 
     * @return
     *     possible object is
     *     {@link ObjArrayField }
     *     
     */
    public ObjArrayField getObjArrayField() {
        return objArrayField;
    }

    /**
     * Sets the value of the objArrayField property.
     * 
     * @param value
     *     allowed object is
     *     {@link ObjArrayField }
     *     
     */
    public void setObjArrayField(ObjArrayField value) {
        this.objArrayField = value;
    }

    /**
     * Gets the value of the objField property.
     * 
     * @return
     *     possible object is
     *     {@link ObjField }
     *     
     */
    public ObjField getObjField() {
        return objField;
    }

    /**
     * Sets the value of the objField property.
     * 
     * @param value
     *     allowed object is
     *     {@link ObjField }
     *     
     */
    public void setObjField(ObjField value) {
        this.objField = value;
    }

    /**
     * Gets the value of the simpleArrayField property.
     * 
     * @return
     *     possible object is
     *     {@link SimpleArrayField }
     *     
     */
    public SimpleArrayField getSimpleArrayField() {
        return simpleArrayField;
    }

    /**
     * Sets the value of the simpleArrayField property.
     * 
     * @param value
     *     allowed object is
     *     {@link SimpleArrayField }
     *     
     */
    public void setSimpleArrayField(SimpleArrayField value) {
        this.simpleArrayField = value;
    }

    /**
     * Gets the value of the simpleField property.
     * 
     * @return
     *     possible object is
     *     {@link SimpleField }
     *     
     */
    public SimpleField getSimpleField() {
        return simpleField;
    }

    /**
     * Sets the value of the simpleField property.
     * 
     * @param value
     *     allowed object is
     *     {@link SimpleField }
     *     
     */
    public void setSimpleField(SimpleField value) {
        this.simpleField = value;
    }

    /**
     * Gets the value of the parent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParent() {
        return parent;
    }

    /**
     * Sets the value of the parent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParent(String value) {
        this.parent = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

}
