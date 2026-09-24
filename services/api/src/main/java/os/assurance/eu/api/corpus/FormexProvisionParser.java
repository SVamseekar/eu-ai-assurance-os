package os.assurance.eu.api.corpus;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public final class FormexProvisionParser {
  public List<ParsedProvision> parse(String celex, byte[] formexXml) {
    Document document = read(formexXml);
    List<ParsedProvision> provisions = new ArrayList<>();
    walk(document.getDocumentElement(), celex, provisions);
    return List.copyOf(provisions);
  }

  private static void walk(Node node, String celex, List<ParsedProvision> provisions) {
    Node current = node;
    while (current != null) {
      if (current instanceof Element element) {
        String name = localName(element);
        if ("ARTICLE".equals(name)) {
          provisions.addAll(article(celex, element));
        } else if ("ANNEX".equals(name)) {
          provisions.add(annex(celex, element));
        } else {
          walk(element.getFirstChild(), celex, provisions);
        }
      }
      current = current.getNextSibling();
    }
  }

  private static List<ParsedProvision> article(String celex, Element article) {
    String articleNo = number(attr(article, "IDENTIFIER"));
    List<Element> paragraphs = children(article, "PARAG");
    if (paragraphs.isEmpty()) {
      return List.of(provision(celex, articleNo, "", "", "", alineaText(article, false)));
    }
    List<ParsedProvision> parsed = new ArrayList<>();
    for (Element paragraph : paragraphs) {
      String paragraphNo = number(attr(paragraph, "IDENTIFIER"));
      List<Element> points = children(paragraph, "POINT");
      if (points.isEmpty()) {
        parsed.add(provision(celex, articleNo, paragraphNo, "", "", alineaText(paragraph, false)));
      } else {
        for (Element point : points) {
          parsed.add(provision(
              celex,
              articleNo,
              paragraphNo,
              attr(point, "IDENTIFIER"),
              "",
              alineaText(point, false)));
        }
      }
    }
    return parsed;
  }

  private static ParsedProvision annex(String celex, Element annex) {
    String annexId = annexId(attr(annex, "IDENTIFIER"));
    return provision(celex, "", "", "", annexId, alineaText(annex, false));
  }

  private static ParsedProvision provision(
      String celex,
      String article,
      String paragraph,
      String point,
      String annex,
      String text) {
    String slot = annex == null || annex.isBlank() ? article : annex;
    String key = celex + "#" + slot + ":" + paragraph + ":" + point;
    return new ParsedProvision(key, article, paragraph, point, annex, text.trim());
  }

  private static String annexId(String identifier) {
    String raw = identifier == null ? "" : identifier.trim();
    if (raw.regionMatches(true, 0, "ANX_", 0, 4)) {
      raw = raw.substring(4);
    }
    return "annex" + raw;
  }

  private static String number(String identifier) {
    if (identifier == null || identifier.isBlank()) {
      return "";
    }
    String raw = identifier.trim();
    int index = 0;
    while (index < raw.length() - 1 && raw.charAt(index) == '0') {
      index++;
    }
    return raw.substring(index);
  }

  private static String alineaText(Element root, boolean skipNestedPoints) {
    StringBuilder text = new StringBuilder();
    collectAlineas(root, text, skipNestedPoints);
    return text.toString().replaceAll("\\s+", " ").trim();
  }

  private static void collectAlineas(Node node, StringBuilder text, boolean skipNestedPoints) {
    Node current = node.getFirstChild();
    while (current != null) {
      if (current instanceof Element element) {
        String name = localName(element);
        if (skipNestedPoints && "POINT".equals(name)) {
          current = current.getNextSibling();
          continue;
        }
        if ("ALINEA".equals(name)) {
          if (!text.isEmpty()) {
            text.append(' ');
          }
          text.append(element.getTextContent());
        } else {
          collectAlineas(element, text, skipNestedPoints);
        }
      }
      current = current.getNextSibling();
    }
  }

  private static List<Element> children(Element parent, String name) {
    List<Element> found = new ArrayList<>();
    Node current = parent.getFirstChild();
    while (current != null) {
      if (current instanceof Element element && name.equals(localName(element))) {
        found.add(element);
      }
      current = current.getNextSibling();
    }
    return found;
  }

  private static String attr(Element element, String name) {
    return element.hasAttribute(name) ? element.getAttribute(name) : "";
  }

  private static String localName(Element element) {
    String local = element.getLocalName();
    return local == null || local.isBlank() ? element.getTagName() : local;
  }

  private static Document read(byte[] xml) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setExpandEntityReferences(false);
      return factory.newDocumentBuilder().parse(new InputSource(new ByteArrayInputStream(xml)));
    } catch (Exception ex) {
      throw new IllegalArgumentException("Formex XML could not be parsed", ex);
    }
  }
}
