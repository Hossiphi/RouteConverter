//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0.5-b02-fcs 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2008.10.12 at 02:39:09 PM CEST 
//


package slash.navigation.kml.binding22beta;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for StyleMapType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StyleMapType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://earth.google.com/kml/2.2}AbstractStyleSelectorType">
 *       &lt;sequence>
 *         &lt;element ref="{http://earth.google.com/kml/2.2}Pair" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StyleMapType", propOrder = {
    "pair"
})
public class StyleMapType
    extends AbstractStyleSelectorType
{

    @XmlElement(name = "Pair")
    protected List<PairType> pair;

    /**
     * Gets the value of the pair property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the pair property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPair().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PairType }
     * 
     * 
     */
    public List<PairType> getPair() {
        if (pair == null) {
            pair = new ArrayList<PairType>();
        }
        return this.pair;
    }

}
