//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.10.07 at 09:27:50 PM CEST 
//


package slash.navigation.kml.binding21;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for ChangeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ChangeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element ref="{http://earth.google.com/kml/2.1}Object" minOccurs="0"/>
 *         &lt;element ref="{http://earth.google.com/kml/2.1}Feature" minOccurs="0"/>
 *         &lt;element ref="{http://earth.google.com/kml/2.1}Geometry" minOccurs="0"/>
 *         &lt;element ref="{http://earth.google.com/kml/2.1}StyleSelector" minOccurs="0"/>
 *         &lt;element ref="{http://earth.google.com/kml/2.1}TimePrimitive" minOccurs="0"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChangeType", propOrder = {
    "objectOrFeatureOrGeometry"
})
public class ChangeType {

    @XmlElementRefs({
        @XmlElementRef(name = "Geometry", namespace = "http://earth.google.com/kml/2.1", type = JAXBElement.class),
        @XmlElementRef(name = "TimePrimitive", namespace = "http://earth.google.com/kml/2.1", type = JAXBElement.class),
        @XmlElementRef(name = "StyleSelector", namespace = "http://earth.google.com/kml/2.1", type = JAXBElement.class),
        @XmlElementRef(name = "Object", namespace = "http://earth.google.com/kml/2.1", type = JAXBElement.class),
        @XmlElementRef(name = "Feature", namespace = "http://earth.google.com/kml/2.1", type = JAXBElement.class)
    })
    protected List<JAXBElement<? extends ObjectType>> objectOrFeatureOrGeometry;

    /**
     * Gets the value of the objectOrFeatureOrGeometry property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the objectOrFeatureOrGeometry property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getObjectOrFeatureOrGeometry().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link LinearRingType }{@code >}
     * {@link JAXBElement }{@code <}{@link MultiGeometryType }{@code >}
     * {@link JAXBElement }{@code <}{@link LocationType }{@code >}
     * {@link JAXBElement }{@code <}{@link ScreenOverlayType }{@code >}
     * {@link JAXBElement }{@code <}{@link StyleMapType }{@code >}
     * {@link JAXBElement }{@code <}{@link ScaleType }{@code >}
     * {@link JAXBElement }{@code <}{@link PolyStyleType }{@code >}
     * {@link JAXBElement }{@code <}{@link StyleType }{@code >}
     * {@link JAXBElement }{@code <}{@link BalloonStyleType }{@code >}
     * {@link JAXBElement }{@code <}{@link PointType }{@code >}
     * {@link JAXBElement }{@code <}{@link GroundOverlayType }{@code >}
     * {@link JAXBElement }{@code <}{@link TimePrimitiveType }{@code >}
     * {@link JAXBElement }{@code <}{@link FolderType }{@code >}
     * {@link JAXBElement }{@code <}{@link TimeStampType }{@code >}
     * {@link JAXBElement }{@code <}{@link DocumentType }{@code >}
     * {@link JAXBElement }{@code <}{@link ObjectType }{@code >}
     * {@link JAXBElement }{@code <}{@link ListStyleType }{@code >}
     * {@link JAXBElement }{@code <}{@link FeatureType }{@code >}
     * {@link JAXBElement }{@code <}{@link LabelStyleType }{@code >}
     * {@link JAXBElement }{@code <}{@link GeometryType }{@code >}
     * {@link JAXBElement }{@code <}{@link NetworkLinkType }{@code >}
     * {@link JAXBElement }{@code <}{@link LinkType }{@code >}
     * {@link JAXBElement }{@code <}{@link RegionType }{@code >}
     * {@link JAXBElement }{@code <}{@link StyleSelectorType }{@code >}
     * {@link JAXBElement }{@code <}{@link PlacemarkType }{@code >}
     * {@link JAXBElement }{@code <}{@link LatLonBoxType }{@code >}
     * {@link JAXBElement }{@code <}{@link LinkType }{@code >}
     * {@link JAXBElement }{@code <}{@link LookAtType }{@code >}
     * {@link JAXBElement }{@code <}{@link PolygonType }{@code >}
     * {@link JAXBElement }{@code <}{@link LodType }{@code >}
     * {@link JAXBElement }{@code <}{@link LineStringType }{@code >}
     * {@link JAXBElement }{@code <}{@link OrientationType }{@code >}
     * {@link JAXBElement }{@code <}{@link IconStyleType }{@code >}
     * {@link JAXBElement }{@code <}{@link ModelType }{@code >}
     * {@link JAXBElement }{@code <}{@link TimeSpanType }{@code >}
     * {@link JAXBElement }{@code <}{@link LineStyleType }{@code >}
     * 
     * 
     */
    public List<JAXBElement<? extends ObjectType>> getObjectOrFeatureOrGeometry() {
        if (objectOrFeatureOrGeometry == null) {
            objectOrFeatureOrGeometry = new ArrayList<JAXBElement<? extends ObjectType>>();
        }
        return this.objectOrFeatureOrGeometry;
    }

}
