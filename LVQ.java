
public class LVQ extends Classifier {
	public enum LVQIniMethod
	{
		Random,
		FirstValues
	}

	int neuronsCount;
	int i = 65; //Class Index
	VectorNeural[] inputNeurons;
	VectorNeural[] outputNeurons;
	private double learnRate;
	private LVQNeuron[] neurons;
	
	///public DataSet validateSet;
	
	private LVQIniMethod iniMethod = LVQIniMethod.FirstValues;
	
	static final VectorNeural.DistanceMethod DEFAULT_DISTANCE_METHOD = VectorNeural.DistanceMethod.MANHATTAN;
	
	public LVQ(double learnRate, int neuronsCount, LVQIniMethod iniMethod)
	{
		this.learnRate = learnRate;
		this.neuronsCount = neuronsCount;
		this.iniMethod = iniMethod;
	}
	
	private void initializeNeurons(DataSet trainSet) {
		// Inicializa em posicoes randomicas
		neurons = new LVQNeuron[(trainSet.class_count)*neuronsCount];
		int countNeuronsFromClass = 0;
		int actualClassIndex = 0;
		for (int i = 0; i < neurons.length; i++) {
			// Change class index
			if (countNeuronsFromClass < neuronsCount) {
				countNeuronsFromClass++;
			} else {
				actualClassIndex++;
				countNeuronsFromClass = 1;
			}
			
			neurons[i] = new LVQNeuron(actualClassIndex, trainSet.attrib_count);
		}
		
		//Se for First Values, procura os primeiros valores de cada classe
		if(iniMethod == LVQIniMethod.FirstValues)
		{
			trainSet.reset();
			for(int i = 0; i < neurons.length; i++)
			{
				LVQNeuron neuron = neurons[i];
				
				//Se for um neuronio de classe diferente do anterior, volta a contar do inicio
				if(i-1 > -1)
				{
					if(neuron._class != neurons[i-1]._class)
					{
						trainSet.reset();
					}
				}
				
				//Posiciona neuronio na posicao da primeira entrada encontrada
				while(trainSet.hasNext())
				{
					LVQNeuron in = new LVQNeuron(trainSet.next(), trainSet.classAttributteIndex);
					if(in._class == neuron._class)
					{
						neuron.vector = in.vector;
						break;
					}
				}
			}
		}
		
	}

	@Override
	public void train(DataSet trainSet, DataSet validateSet) {
		System.out.println("Training");
		
		// PASSO 0 inicializar todos os pesos... K-means ou random
		initializeNeurons(trainSet);
		
		
		/*
			x – vetor de treinamento (x1, ..., xi, ..., xn)
			T – classe correta para o vetor de treinamento
			wj – vetor peso da j-ésima unidade de saída (w1,j, ..., wi,j, ..., wn,j)
			Cj – classe representada pela j-ésima unidade de saída
			║ x – wj ║ - distância Euclidiana entre o vetor de entrada e (vetor de pesos para) o j-ésimo vetor de saída.
			J - uma unidade de saída
		*/
		
		// 1- Enquanto condicao de parada eh falsa execute os passos 2-6
		int EpochsCounter = 0;
		double actualLearnRate = learnRate;
		double reductionRate = 30;
		
		//Erros
		double actualError = validate(validateSet);
		double lastError = actualError;
		System.out.println("Erro inicial (pos-inicializacao): " + actualError);
		
		while (actualError <= lastError /*&& EpochsCounter < 1000*/) {
			
			if(actualLearnRate <= 0.0)
			{
				System.out.print("TAXA MENOR QUE ZERO!");
				break;
			}
			
			System.out.println("Epoca: " + EpochsCounter +" Taxa de aprendizado em: " + actualLearnRate);
			//2- Para cada vetor de entrada de treinamento, executar os passos 3-4
			while (trainSet.hasNext()) {
				//System.out.println("Treinando linha " + line + " do DataSet");
				
				//3- Encontre a unidade de saida J tal que | x - Wj | seja minima
				LVQNeuron selectedNeuron = neurons[0];
				LVQNeuron neuronDataLine = new LVQNeuron(trainSet.next(), trainSet.classAttributteIndex);

				double minDistance = Double.MAX_VALUE;
				double temp;
				for (int i = 0; i < neurons.length; i++) {
					temp = neurons[i].distanceFrom(neuronDataLine);
					if (temp < minDistance) {
						minDistance = temp;
						selectedNeuron = neurons[i];
					}
				}
				
				/*4- Altere Wj como na regra abaixo
					Se T = CJ, então
						wJ(new) = wJ(old) + α[x – wJ(old)];
					Se T ≠ CJ, então
						wJ(new) = wJ(old) - α[x – wJ(old)]; 
				 */
				if (selectedNeuron._class == neuronDataLine._class) {
					selectedNeuron.aproach(neuronDataLine, actualLearnRate);
					//double distance = selectedNeuron.distanceFrom(neuronDataLine);
					//System.out.println(distance);
				} else {
					selectedNeuron.diverge(neuronDataLine, actualLearnRate);
					//double distance = selectedNeuron.distanceFrom(neuronDataLine);
					//System.out.println(distance);
				}
			}
			
			// Reseta Train set para um possivel novo treinamento
			trainSet.reset();
			
			// 5 - Reduza a taxa de aprendizado (?) como?
			//Reduz linearmente
			//actualLearnRate = learnRate * ((double)(stopCondition - EpochsCounter)/(double)stopCondition);
			actualLearnRate = learnRate * Math.pow(Math.E, -1 * (EpochsCounter/reductionRate));
			
			
			/* 6
				Teste a condição de parada
				A condição deve especificar um número fixo de iterações (i.e.,execução do Passo 1) 
				ou um valor mínimo para a taxa de aprendizado. 
			 */
			
			
			
			double error = validate(validateSet);
			logError(EpochsCounter, error);
			
			
			EpochsCounter++;
			//Atualiza os erros (a cada 10 epocas)
			if(EpochsCounter % 15 == 0)
			{
				lastError = actualError;
				actualError = error;
				System.out.println("validando: " + actualError);
			}
			
			
				
		}
	}

	@Override
	public double validate(DataSet validateSet) {
		//System.out.println("Validating");
		
		double totalError = 0;
		validateSet.reset();
		while(validateSet.hasNext())
		{
			boolean found = false;
			double minDistance = 0.0;
			LVQNeuron input = new LVQNeuron(validateSet.next(), validateSet.classAttributteIndex);
			for (int i = 0; i < neurons.length; i++) {
				if(input._class == neurons[i]._class)
				{
					double distance = input.distanceFrom(neurons[i]);
					if(!found)
						minDistance = distance;
					else if(minDistance > distance)
					{
						minDistance = distance;
					}
					
					found = true;
				}
			}
			
			if(found)
				totalError += minDistance;
			else
				System.out.println("Class not found!" + input._class);
		}
		return totalError;
	}

	@Override
	public void test(DataSet testSet) {
		System.out.println("Testing");
		TestData test = new TestData(testSet.class_count);
		//testSet.printClassDistribution(testSet.classAttributteIndex);
		while (testSet.hasNext()) {
			LVQNeuron selectedNeuron = neurons[0];
			LVQNeuron neuronDataLine = new LVQNeuron(testSet.next(), testSet.classAttributteIndex);

			double minDistance = Double.MAX_VALUE;
			double temp;
			for (int i = 0; i < neurons.length; i++) {
				temp = neurons[i].distanceFrom(neuronDataLine);
				if (temp < minDistance) {
					minDistance = temp;
					selectedNeuron = neurons[i];
				}
			}
			
			test.test(selectedNeuron._class , neuronDataLine._class);
		}
		
		test.printResults();
	}
}
