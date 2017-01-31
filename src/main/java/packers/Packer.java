package packers;

import analysers.analysable.AstMethod;
import analysers.analysable.DaikonMethod;
import analysers.analysable.MethodDescription;
import analysers.analysable.AsmType;
import com.github.javaparser.ast.comments.JavadocComment;
import org.javatuples.Pair;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public final class Packer {

    private Packer() {

    }

    private static final String methodsTagName = "methods";
    private static final String methodTagName = "method";
    private static final String javaDocTagName = "javaDoc";
    private static final String headTagName = "head";
    private static final String paramTagName = "param";
    private static final String returnTagName = "return";
    private static final String seeTagName = "see";
    private static final String throwsTagName = "throws";
    private static final String descriptionTagName = "description";
    private static final String nameTagName = "name";
    private static final String typeTagName = "type";
    private static final String parametersTagName = "parameters";
    private static final String ownerTagName = "owner";
    private static final String contractTagName = "contract";
    private static final String enterTagName = "enter";
    private static final String entersTagName = "enters";
    private static final String exitTagName = "exit";
    private static final String exitsTagName = "exits";
    private static final String exitIdTagName = "exitId";
    private static final String exitIdsTagName = "exitIds";

    public static void packMethods(String fileName, Collection<Pair<AstMethod, DaikonMethod>> methods) throws IOException, XMLStreamException {
        final XMLOutputFactory output = XMLOutputFactory.newInstance();
        final XMLStreamWriter writer = output.createXMLStreamWriter(new FileWriter(fileName));
        writer.writeStartDocument();
        writer.writeStartElement(methodsTagName);
        for (Pair<AstMethod, DaikonMethod> method : methods) {
            final AstMethod astMethod = method.getValue0();
            final DaikonMethod daikonMethod = method.getValue1();
            final MethodDescription description = astMethod == null ? daikonMethod.getDescription() : astMethod.getDescription();

            writer.writeStartElement(methodTagName);

            if (astMethod != null) {
                final JavadocComment doc = astMethod.getJavadocComment();
                if (doc != null) {
                    final List<String> tokens = Arrays.stream(doc.getContent().split("(\n(\\s|\t)*/?\\*|\t|\\s)+"))
                            .filter(token -> token.length() > 0)
                            .collect(Collectors.toList());
                    if (tokens.size() == 0 || tokens.get(0).length() == 0) continue;
                    writer.writeStartElement(javaDocTagName);
                    writer.writeStartElement(Packer.getTag(tokens.get(0)));
                    writer.writeCharacters(tokens.get(0) + " ");
                    for (String token : tokens.subList(1, tokens.size())) {
                        String tag = Packer.getTag(token);
                        if (!tag.equals(headTagName)) {
                            writer.writeEndElement();
                            writer.writeStartElement(tag);
                        }
                        writer.writeCharacters(token + " ");
                    }
                    writer.writeEndElement();
                    writer.writeEndElement();
                }
            }

            writer.writeStartElement(descriptionTagName);
            writer.writeStartElement(nameTagName);
            writer.writeCharacters(description.getName());
            writer.writeEndElement();
            writer.writeStartElement(typeTagName);
            writer.writeCharacters(description.getType().getName());
            writer.writeEndElement();
            writer.writeStartElement(parametersTagName);
            for (Pair<AsmType, String> type : description.getParameters()) {
                writer.writeStartElement(paramTagName);
                writer.writeStartElement(typeTagName);
                writer.writeCharacters(type.getValue0().getName());
                writer.writeEndElement();
                writer.writeStartElement(nameTagName);
                writer.writeCharacters(type.getValue1());
                writer.writeEndElement();
                writer.writeEndElement();
            }
            writer.writeEndElement();
            writer.writeStartElement(ownerTagName);
            writer.writeCharacters(description.getOwner().getName());
            writer.writeEndElement();
            writer.writeEndElement();

            if (daikonMethod != null) {
                writer.writeStartElement(contractTagName);
                writer.writeStartElement(entersTagName);
                for (String enter : daikonMethod.enter) {
                    writer.writeStartElement(enterTagName);
                    writer.writeCharacters(enter);
                    writer.writeEndElement();
                }
                writer.writeEndElement();
                writer.writeStartElement(exitsTagName);
                for (String exit : daikonMethod.exit) {
                    writer.writeStartElement(exitTagName);
                    writer.writeCharacters(exit);
                    writer.writeEndElement();
                }
                writer.writeEndElement();
                writer.writeStartElement(exitIdsTagName);
                for (Map.Entry<Integer, Set<String>> entry : daikonMethod.exits.entrySet()) {
                    writer.writeStartElement(exitIdTagName);
                    writer.writeCharacters(entry.getKey().toString());
                    writer.writeEndElement();
                    writer.writeStartElement(exitsTagName);
                    for (String exit : entry.getValue()) {
                        writer.writeStartElement(exitTagName);
                        writer.writeCharacters(exit);
                        writer.writeEndElement();
                    }
                    writer.writeEndElement();
                }
                writer.writeEndElement();
                writer.writeEndElement();
            }

            writer.writeEndElement();
        }
        writer.writeEndElement();
        writer.close();
    }

    private static String getTag(String token) {
        switch (token) {
            case "@return": return returnTagName;
            case "@param": return paramTagName;
            case "@see": return seeTagName;
            case "@throws": return throwsTagName;
            default: return headTagName;
        }
    }
}