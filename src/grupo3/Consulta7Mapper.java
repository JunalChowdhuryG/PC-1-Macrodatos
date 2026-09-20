package grupo3;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

/**
 * Consulta 7 (Grupo 3 - busqueda de subtexto en varios campos)
 * Pregunta: Que registros de IIEE contienen el subtexto "SAN" en su nombre
 * de institucion educativa, su direccion, o su centro poblado?
 *
 * Es un job de tipo FILTRO: no se agrega nada, solo se deja pasar la fila
 * completa cuando hay coincidencia en cualquiera de los 3 campos de texto.
 */
public class Consulta7Mapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {

	// Cambiar este valor para buscar otro subtexto sin tocar el resto del codigo
	private static final String SUBTEXTO = "SAN";

	public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
		if (key.get() == 0) {
			return;
		}

		String linea = value.toString();
		String[] campos = linea.split(";", -1);
		if (campos.length != 18) {
			return;
		}

		String codigoIIEEQW = campos[7].trim();
		String institucionEducativa = campos[10].trim().toUpperCase();
		String direccionIIEE = campos[13].trim().toUpperCase();
		String centroPoblado = campos[5].trim().toUpperCase();

		boolean coincide = institucionEducativa.contains(SUBTEXTO)
			|| direccionIIEE.contains(SUBTEXTO)
			|| centroPoblado.contains(SUBTEXTO);

		if (coincide) {
			// Se usa el codigo de IIEE como llave y la fila completa como
			// valor, para poder mostrar el registro entero en el resultado
			output.collect(new Text(codigoIIEEQW), value);
		}
	}
}
