package grupo6;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

/**
 * Igual que SumReducer del Grupo 1, pero para valores decimales (DoubleWritable).
 * Se reutiliza en cada epoca de las Consultas 11 y 12 para sumar los
 * gradientes parciales, la perdida y el conteo de aciertos de todas las
 * filas que procesa el Mapper.
 */
public class SumDoubleReducer extends MapReduceBase implements Reducer<Text, DoubleWritable, Text, DoubleWritable> {

	public void reduce(Text t_key, Iterator<DoubleWritable> values, OutputCollector<Text, DoubleWritable> output, Reporter reporter) throws IOException {
		double suma = 0.0;
		while (values.hasNext()) {
			suma += values.next().get();
		}
		output.collect(t_key, new DoubleWritable(suma));
	}
}
