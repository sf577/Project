package lsi.noc.kernel.testbench;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import lsi.noc.kernel.SimpleCommunication;
import lsi.noc.kernel.SimpleTask;
import lsi.noc.kernel.priority.PriorityTrafficFlow;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;

public class MCSLBenchmark {

	DirectedGraph<SimpleTask, String> taskGraph;
	ArrayList<SimpleCommunication> flows;
	int currentBenchmark;

	public MCSLBenchmark(int n) {
		taskGraph = new DirectedSparseGraph<SimpleTask, String>();
		flows = new ArrayList<SimpleCommunication>();
		currentBenchmark = n;
		createBenchmark(n);
		visualizeGraph();
		debugCommunication();

		// generateModel();
	}

	public void createBenchmark(int n) {

		String s = getBenchmarkString(n);

		StringTokenizer st = new StringTokenizer(s);

		// parse the header
		st.nextToken();
		st.nextToken(); // skip the type of trace and the number of cores
		int ntasks = Integer.parseInt(st.nextToken());
		int nflows = Integer.parseInt(st.nextToken());
		int niterations = Integer.parseInt(st.nextToken());

		// create tasks with worst case execution
		for (int i = 0; i < ntasks; i++) {

			String taskid = "t" + st.nextToken();
			int wcet = 0;

			st.nextToken();// discard mapping information

			for (int j = 0; j < niterations; j++)
				st.nextToken(); // discard schedule sequence numbers

			for (int j = 0; j < niterations; j++) {
				int twcet = Integer.parseInt(st.nextToken());
				if (twcet > wcet)
					wcet = twcet; // stores the worst case execution time
			}

			SimpleTask t = new SimpleTask(taskid);
			t.setCost(wcet);
			taskGraph.addVertex(t); // adds task to taskgraph

		}

		// create flows with worst case transmission
		for (int i = 0; i < nflows; i++) {

			String flowid = "f" + st.nextToken();
			String senderid = "t" + st.nextToken();
			String receiverid = "t" + st.nextToken();

			st.nextToken();
			st.nextToken(); // discard mapping information
			for (int j = 0; j < niterations; j++) {
				st.nextToken();
				st.nextToken();
			} // discard memory addresses

			int maxvol = 0;

			for (int j = 0; j < niterations; j++) {
				int tmaxvol = Integer.parseInt(st.nextToken());
				if (tmaxvol > maxvol)
					maxvol = tmaxvol; // stores the maximum communication volume
			}

			SimpleCommunication f = new SimpleCommunication(getTask(senderid),
					getTask(receiverid));
			f.setVolume(maxvol);
			f.setName(flowid);

			// System.out.println("new edge: "+flowid+" "+maxvol+" sender: "+senderid+"  rec:"+receiverid);
			taskGraph.addEdge(flowid + " " + maxvol, getTask(senderid),
					getTask(receiverid)); // adds edge to taskgraph
			flows.add(f); // adds communication to vector
		}
	}

	private String getBenchmarkString(int n) {

		String st = "";

		if (n == 1) {
			// data from h264dl_mesh_4x4.rtp version 1.1
			st = "1	16	51	71	10 "
					+ "0	0	0	1	3	6	10	15	21	28	36	44	4343	4744	4385	4802	4286	4214	4386	4526	4986	5040	 "
					+ "1	0	2	4	7	11	16	22	29	37	45	52	2865	2460	2438	2521	2369	2591	2407	2611	2486	2498	 "
					+ "2	0	5	8	12	17	23	30	38	46	53	59	4315	4806	4231	4161	4194	4515	4558	4190	4750	4433	 "
					+ "3	0	9	13	18	24	31	39	47	54	60	65	3240	2933	3040	2996	3189	3135	3157	2888	3306	3346	 "
					+ "4	0	14	19	25	32	40	48	55	61	66	70	3771	3570	3608	3197	3615	3627	3538	3593	3960	3548	 "
					+ "5	1	0	2	6	10	14	18	22	26	30	34	3524	3161	3163	3451	3812	3794	3457	3635	3754	3438	 "
					+ "6	2	0	2	6	10	14	18	22	26	30	34	3890	3470	3370	3151	3591	3613	3532	3637	3538	3407	 "
					+ "7	3	0	2	6	10	14	18	22	26	30	34	3615	3776	3178	3848	3367	3294	3456	3560	3610	3867	 "
					+ "8	4	0	2	6	10	14	18	22	26	30	34	3620	3822	3709	3595	3455	3537	3231	3618	3735	3534	 "
					+ "9	5	0	2	6	10	14	18	22	26	30	34	3755	3349	3665	3230	3960	3385	3960	3164	3574	3061	 "
					+ "10	6	0	2	6	10	14	18	22	26	30	34	3459	3587	3543	3023	3734	3663	3352	3651	3101	3762	 "
					+ "11	7	0	2	6	10	14	18	22	26	30	34	3140	3445	3878	2979	3685	3554	3406	3503	3597	3529	 "
					+ "12	8	0	2	6	10	14	18	22	26	30	34	3661	3906	3284	3752	3928	3736	3207	3321	3728	3747	 "
					+ "13	9	0	2	6	10	14	18	22	26	30	34	3501	3787	3414	3960	3315	3678	3960	3475	3490	3423	 "
					+ "14	10	0	2	6	10	14	18	22	26	30	34	3400	3689	3828	3323	3311	3662	3320	3782	3446	3505	 "
					+ "15	0	20	26	33	41	49	56	62	67	71	74	3652	3401	3888	3758	3092	3444	3346	3674	3553	3401	 "
					+ "16	1	1	3	7	11	15	19	23	27	31	35	3143	3174	3248	3163	3485	3593	3846	3620	3683	3348	 "
					+ "17	2	1	3	7	11	15	19	23	27	31	35	3582	3566	3536	3264	3554	3543	3200	3821	3586	3611	 "
					+ "18	3	1	3	7	11	15	19	23	27	31	35	3774	3713	3598	3612	3061	3333	3787	3660	3400	3321	 "
					+ "19	4	1	3	7	11	15	19	23	27	31	35	3546	3653	3607	3665	3197	3448	3510	3397	3470	3598	 "
					+ "20	5	1	3	7	11	15	19	23	27	31	35	3134	3725	3399	3588	3479	3619	3298	3413	3551	3542	 "
					+ "21	6	1	3	7	11	15	19	23	27	31	35	3822	3759	3682	3960	3160	3682	3394	3301	3457	3475	 "
					+ "22	7	1	3	7	11	15	19	23	27	31	35	3703	3603	3494	3524	3409	3331	3565	3644	3727	3601	 "
					+ "23	8	1	3	7	11	15	19	23	27	31	35	3491	3518	3620	3702	3926	3596	3089	3589	3663	3947	 "
					+ "24	9	1	3	7	11	15	19	23	27	31	35	3602	3610	3435	3311	3535	3189	3334	3415	3739	3362	 "
					+ "25	10	1	3	7	11	15	19	23	27	31	35	3531	3960	3463	3453	3414	3150	3219	3701	3429	3577	 "
					+ "26	0	35	43	51	58	64	69	73	76	78	79	3378	3730	3538	3696	3515	3622	3507	3429	3424	3618	 "
					+ "27	1	5	9	13	17	21	25	29	33	37	39	3430	3518	3588	3634	3477	3465	3742	3683	3230	3738	 "
					+ "28	2	5	9	13	17	21	25	29	33	37	39	3603	3575	3285	3091	3095	3895	3398	3651	3671	3572	 "
					+ "29	3	5	9	13	17	21	25	29	33	37	39	3687	3491	3746	3498	3695	3802	3345	3492	3485	3264	 "
					+ "30	4	5	9	13	17	21	25	29	33	37	39	3411	3726	3737	3636	3510	3507	3812	3351	3576	3560	 "
					+ "31	5	5	9	13	17	21	25	29	33	37	39	3425	3347	3590	3342	3362	3418	3654	3632	3486	3630	 "
					+ "32	6	5	9	13	17	21	25	29	33	37	39	3366	3916	3064	3388	3401	3394	3606	3369	3337	3262	 "
					+ "33	7	5	9	13	17	21	25	29	33	37	39	3881	3699	3331	3200	3408	3682	3660	3481	3154	3618	 "
					+ "34	8	5	9	13	17	21	25	29	33	37	39	3237	3775	3318	3666	3756	3651	3558	3608	3601	3912	 "
					+ "35	9	5	9	13	17	21	25	29	33	37	39	3425	3205	3476	3554	3152	3418	3404	3267	3694	3422	 "
					+ "36	10	5	9	13	17	21	25	29	33	37	39	3603	3257	3458	3353	3462	3196	3770	3706	3426	3470	 "
					+ "37	11	0	3	6	9	12	15	18	21	24	27	3091	3466	3087	3202	3214	3233	3157	3137	3093	3262	 "
					+ "38	11	1	4	7	10	13	16	19	22	25	28	2671	2755	2717	2406	2462	2481	2637	2676	2862	2692	 "
					+ "39	0	27	34	42	50	57	63	68	72	75	77	3346	3333	3452	3485	3489	3385	3803	3166	3678	3374	 "
					+ "40	1	4	8	12	16	20	24	28	32	36	38	3671	3481	3737	3219	3520	3386	3514	3531	3739	3280	 "
					+ "41	2	4	8	12	16	20	24	28	32	36	38	3139	3676	3341	3801	3586	3344	3478	3310	3603	3536	 "
					+ "42	3	4	8	12	16	20	24	28	32	36	38	3722	3922	3494	3960	3432	3422	3398	3347	3960	3582	 "
					+ "43	4	4	8	12	16	20	24	28	32	36	38	3530	3128	3378	3479	3439	3273	3579	3462	3692	3362	 "
					+ "44	5	4	8	12	16	20	24	28	32	36	38	3549	3632	3180	3325	3547	3538	3469	3299	3516	3450	 "
					+ "45	6	4	8	12	16	20	24	28	32	36	38	3640	3398	3415	3622	3848	3650	3522	3384	3737	3960	 "
					+ "46	7	4	8	12	16	20	24	28	32	36	38	3410	3340	3220	3398	3233	3257	3202	3527	3608	3390	 "
					+ "47	8	4	8	12	16	20	24	28	32	36	38	3774	3401	3623	3615	3541	3503	3450	3349	3453	3450	 "
					+ "48	9	4	8	12	16	20	24	28	32	36	38	3314	3594	3112	3471	3945	3423	3536	3576	3770	3610	 "
					+ "49	10	4	8	12	16	20	24	28	32	36	38	3530	3736	3360	3589	3212	3533	3554	3729	3583	3539	 "
					+ "50	11	2	5	8	11	14	17	20	23	26	29	2258	2687	2359	2435	2681	2628	2642	2504	2604	2499	 "
					+ "0	0	1	0	0	0	1	0	1	0	1	0	1	0	1	0	1	0	1	0	1	0	1	0	1	220	217	206	192	185	188	195	196	192	224	 "
					+ "1	1	2	0	0	2	3	2	3	2	3	2	3	2	3	2	3	2	3	2	3	2	3	2	3	210	216	206	213	203	207	199	187	230	200	 "
					+ "2	2	3	0	0	4	5	4	5	4	5	4	5	4	5	4	5	4	5	4	5	4	5	4	5	219	196	213	221	202	172	187	208	193	212	 "
					+ "3	3	4	0	0	6	7	6	7	6	7	6	7	6	7	6	7	6	7	6	7	6	7	6	7	229	188	192	206	196	222	224	180	190	198	 "
					+ "4	3	5	0	1	8	8	8	8	8	8	8	8	8	8	0	0	0	0	0	0	0	0	0	0	189	191	201	214	225	200	194	213	210	217	 "
					+ "5	3	6	0	2	9	9	9	9	9	9	9	9	9	9	0	0	0	0	0	0	0	0	0	0	216	207	195	206	201	189	195	206	200	215	 "
					+ "6	3	7	0	3	10	10	10	10	10	10	10	10	10	10	0	0	0	0	0	0	0	0	0	0	203	217	185	191	205	211	198	202	190	208	 "
					+ "7	3	8	0	4	11	11	11	11	11	11	11	11	11	11	0	0	0	0	0	0	0	0	0	0	203	195	202	212	208	204	204	180	198	192	 "
					+ "8	3	9	0	5	12	12	12	12	12	12	12	12	12	12	0	0	0	0	0	0	0	0	0	0	190	189	210	225	207	208	200	199	214	198	 "
					+ "9	3	10	0	6	13	13	13	13	13	13	13	13	13	13	0	0	0	0	0	0	0	0	0	0	208	208	190	203	201	186	199	190	204	189	 "
					+ "10	3	11	0	7	14	14	14	14	14	14	14	14	14	14	0	0	0	0	0	0	0	0	0	0	220	189	188	200	230	230	212	202	194	197	 "
					+ "11	3	12	0	8	15	15	15	15	15	15	15	15	15	15	0	0	0	0	0	0	0	0	0	0	218	225	202	216	182	223	166	203	204	203	 "
					+ "12	3	13	0	9	16	16	16	16	16	16	16	16	16	16	0	0	0	0	0	0	0	0	0	0	193	223	186	191	201	170	192	200	192	202	 "
					+ "13	3	14	0	10	17	17	17	17	17	17	17	17	17	17	0	0	0	0	0	0	0	0	0	0	195	216	208	191	222	208	205	202	186	200	 "
					+ "14	4	15	0	0	18	19	18	19	18	19	18	19	18	19	18	19	18	19	18	19	18	19	18	19	214	216	211	209	176	181	206	200	207	194	 "
					+ "15	5	16	1	1	1	2	3	1	2	3	1	2	3	1	1	2	3	1	2	3	1	2	3	1	182	213	188	217	189	205	230	183	190	213	 "
					+ "16	6	17	2	2	1	2	1	2	1	2	1	2	1	2	1	2	1	2	1	2	1	2	1	2	208	178	212	199	213	199	205	218	202	225	 "
					+ "17	7	18	3	3	1	2	3	1	2	3	1	2	3	1	1	2	3	1	2	3	1	2	3	1	211	207	213	194	230	188	191	201	205	223	 "
					+ "18	8	19	4	4	1	2	1	2	1	2	1	2	1	2	1	2	1	2	1	2	1	2	1	2	196	200	222	204	217	200	214	215	212	211	 "
					+ "19	9	20	5	5	1	2	3	1	2	3	1	2	3	1	1	2	3	1	2	3	1	2	3	1	217	212	205	212	210	212	207	204	201	188	 "
					+ "20	10	21	6	6	1	2	3	1	2	3	1	2	3	1	1	2	3	1	2	3	1	2	3	1	174	191	202	229	215	209	204	168	190	190	 "
					+ "21	11	22	7	7	1	2	3	1	2	3	1	2	3	1	1	2	3	1	2	3	1	2	3	1	200	198	188	189	186	207	216	193	205	201	 "
					+ "22	12	23	8	8	1	2	3	1	2	3	1	2	3	1	1	2	3	1	2	3	1	2	3	1	183	182	230	195	194	196	217	220	197	227	 "
					+ "23	13	24	9	9	1	2	3	1	2	3	1	2	3	1	1	2	3	1	2	3	1	2	3	1	213	198	192	212	211	229	207	214	199	205	 "
					+ "24	14	25	10	10	1	2	1	2	1	2	1	2	1	2	1	2	1	2	1	2	1	2	1	2	199	212	213	196	192	189	211	220	195	213	 "
					+ "25	4	37	0	11	20	20	20	20	20	20	20	20	20	20	0	0	0	0	0	0	0	0	0	0	209	205	206	203	189	205	185	201	194	212	 "
					+ "26	5	37	1	11	4	4	4	4	4	4	4	4	4	4	1	1	1	1	1	1	1	1	1	1	211	202	195	185	200	209	209	209	206	169	 "
					+ "27	6	37	2	11	3	3	3	3	3	3	3	3	3	3	2	2	2	2	2	2	2	2	2	2	196	201	206	194	215	194	199	205	199	206	 "
					+ "28	7	37	3	11	4	4	4	4	4	4	4	4	4	4	3	3	3	3	3	3	3	3	3	3	207	223	172	214	210	209	211	191	206	204	 "
					+ "29	8	37	4	11	3	3	3	3	3	3	3	3	3	3	4	4	4	4	4	4	4	4	4	4	196	213	198	211	190	194	204	193	195	211	 "
					+ "30	9	37	5	11	4	4	4	4	4	4	4	4	4	4	5	5	5	5	5	5	5	5	5	5	199	196	205	209	213	216	211	212	222	227	 "
					+ "31	10	37	6	11	4	4	4	4	4	4	4	4	4	4	6	6	6	6	6	6	6	6	6	6	205	201	230	205	201	219	210	184	198	201	 "
					+ "32	11	37	7	11	4	4	4	4	4	4	4	4	4	4	7	7	7	7	7	7	7	7	7	7	206	205	208	211	182	210	182	199	175	201	 "
					+ "33	12	37	8	11	4	4	4	4	4	4	4	4	4	4	8	8	8	8	8	8	8	8	8	8	194	189	217	213	204	194	194	201	200	197	 "
					+ "34	13	37	9	11	4	4	4	4	4	4	4	4	4	4	9	9	9	9	9	9	9	9	9	9	227	205	211	183	210	206	206	199	208	196	 "
					+ "35	14	37	10	11	3	3	3	3	3	3	3	3	3	3	10	10	10	10	10	10	10	10	10	10	204	190	206	203	211	221	200	208	205	209	 "
					+ "36	15	26	0	0	21	22	23	21	22	23	21	22	23	21	21	22	23	21	22	23	21	22	23	21	202	206	202	208	196	215	182	189	185	219	 "
					+ "37	16	27	1	1	5	6	5	6	5	6	5	6	5	6	5	6	5	6	5	6	5	6	5	6	208	219	206	190	207	218	224	199	209	207	 "
					+ "38	17	28	2	2	4	5	6	4	5	6	4	5	6	4	4	5	6	4	5	6	4	5	6	4	180	210	210	228	214	187	204	222	205	194	 "
					+ "39	18	29	3	3	5	6	5	6	5	6	5	6	5	6	5	6	5	6	5	6	5	6	5	6	201	225	208	202	192	192	188	204	223	209	 "
					+ "40	19	30	4	4	4	5	6	4	5	6	4	5	6	4	4	5	6	4	5	6	4	5	6	4	215	203	181	190	199	209	194	192	230	230	 "
					+ "41	20	31	5	5	5	6	5	6	5	6	5	6	5	6	5	6	5	6	5	6	5	6	5	6	193	200	187	185	201	201	209	207	223	204	 "
					+ "42	21	32	6	6	5	6	5	6	5	6	5	6	5	6	5	6	5	6	5	6	5	6	5	6	207	193	191	205	215	199	227	212	218	221	 "
					+ "43	22	33	7	7	5	6	5	6	5	6	5	6	5	6	5	6	5	6	5	6	5	6	5	6	220	186	174	200	209	181	193	207	219	177	 "
					+ "44	23	34	8	8	5	6	5	6	5	6	5	6	5	6	5	6	5	6	5	6	5	6	5	6	198	200	213	209	209	203	207	216	201	199	 "
					+ "45	24	35	9	9	5	6	5	6	5	6	5	6	5	6	5	6	5	6	5	6	5	6	5	6	206	200	215	209	207	208	209	202	208	211	 "
					+ "46	25	36	10	10	4	5	6	4	5	6	4	5	6	4	4	5	6	4	5	6	4	5	6	4	204	198	227	227	208	205	210	210	203	212	 "
					+ "47	37	38	11	11	11	12	11	12	11	12	11	12	11	12	11	12	11	12	11	12	11	12	11	12	195	194	202	220	189	195	214	200	195	212	 "
					+ "48	39	26	0	0	24	25	24	25	24	25	24	25	24	25	24	25	24	25	24	25	24	25	24	25	213	202	180	191	226	199	228	219	178	210	 "
					+ "49	40	27	1	1	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	215	203	212	210	218	199	205	208	194	193	 "
					+ "50	41	28	2	2	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	210	176	212	200	188	195	219	212	228	203	 "
					+ "51	42	29	3	3	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	218	204	193	194	200	197	201	209	205	161	 "
					+ "52	43	30	4	4	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	211	199	205	201	213	204	198	209	192	210	 "
					+ "53	44	31	5	5	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	206	202	222	199	208	197	189	213	193	189	 "
					+ "54	45	32	6	6	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	215	205	197	222	189	193	192	190	199	198	 "
					+ "55	46	33	7	7	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	216	185	212	181	188	210	209	211	189	200	 "
					+ "56	47	34	8	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	214	179	205	194	197	205	171	209	213	194	 "
					+ "57	48	35	9	9	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	209	189	203	216	210	230	192	211	213	183	 "
					+ "58	49	36	10	10	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	7	8	215	196	222	210	210	203	200	200	203	210	 "
					+ "59	38	39	11	0	13	13	13	13	13	13	13	13	13	13	26	26	26	26	26	26	26	26	26	26	214	212	191	205	225	221	217	197	214	191	 "
					+ "60	38	40	11	1	14	14	14	14	14	14	14	14	14	14	9	9	9	9	9	9	9	9	9	9	209	225	207	219	203	209	207	221	215	207	 "
					+ "61	38	41	11	2	15	15	15	15	15	15	15	15	15	15	9	9	9	9	9	9	9	9	9	9	204	212	204	205	193	172	208	193	221	216	 "
					+ "62	38	42	11	3	16	16	16	16	16	16	16	16	16	16	9	9	9	9	9	9	9	9	9	9	212	194	203	198	180	212	226	200	196	209	 "
					+ "63	38	43	11	4	17	17	17	17	17	17	17	17	17	17	9	9	9	9	9	9	9	9	9	9	209	181	203	199	207	204	200	203	220	213	 "
					+ "64	38	44	11	5	18	18	18	18	18	18	18	18	18	18	9	9	9	9	9	9	9	9	9	9	204	191	217	196	190	210	185	189	184	188	 "
					+ "65	38	45	11	6	19	19	19	19	19	19	19	19	19	19	9	9	9	9	9	9	9	9	9	9	208	198	194	218	218	205	187	192	210	198	 "
					+ "66	38	46	11	7	20	20	20	20	20	20	20	20	20	20	9	9	9	9	9	9	9	9	9	9	218	183	226	197	209	204	198	216	217	203	 "
					+ "67	38	47	11	8	21	21	21	21	21	21	21	21	21	21	9	9	9	9	9	9	9	9	9	9	202	189	190	198	195	203	215	197	189	199	 "
					+ "68	38	48	11	9	22	22	22	22	22	22	22	22	22	22	9	9	9	9	9	9	9	9	9	9	190	210	205	198	185	217	210	197	196	218	 "
					+ "69	38	49	11	10	23	23	23	23	23	23	23	23	23	23	9	9	9	9	9	9	9	9	9	9	217	186	180	198	219	208	194	195	203	186	 "
					+ "70	38	50	11	11	24	25	24	25	24	25	24	25	24	25	24	25	24	25	24	25	24	25	24	25	201	199	205	202	194	230	223	185	195	214  ";

		}

		return st;

	}

	public void visualizeGraph() {

		// The Layout<V, E> is parameterized by the vertex and edge types
		Layout<SimpleTask, String> layout = new ISOMLayout(taskGraph);
		layout.setSize(new Dimension(900, 900)); // sets the initial size of the
													// space
		// The BasicVisualizationServer<V,E> is parameterized by the edge types
		BasicVisualizationServer<SimpleTask, String> vv = new BasicVisualizationServer<SimpleTask, String>(
				layout);
		vv.setPreferredSize(new Dimension(950, 950)); // Sets the viewing area
														// size

		Transformer<SimpleTask, String> labeler = new Transformer<SimpleTask, String>() {
			public String transform(SimpleTask v) {
				return v.getName();
			}

		};

		vv.getRenderContext().setVertexLabelTransformer(labeler);

		JFrame frame = new JFrame("Simple Graph View");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(vv);
		frame.pack();
		frame.setVisible(true);

	}

	private void debugCommunication() {

		Iterator it = taskGraph.getVertices().iterator();
		while (it.hasNext()) {

			SimpleTask task = (SimpleTask) it.next();
			System.out.println("TASK: " + task.getName());

			for (int i = 0; i < flows.size(); i++) {
				if (flows.get(i).getSender() == task) {
					System.out.println(flows.get(i).getName() + " "
							+ flows.get(i).getReceiver().getName());
				}

			}

		}

		System.out
				.println("===========================================================");

		for (int i = 0; i < taskGraph.getVertices().size(); i++) {

			SimpleTask task = getTask("t" + i);
			System.out
					.println("TASK: " + task.getName() + " " + task.getCost());

			for (int j = 0; j < flows.size(); j++) {
				SimpleCommunication fl = flows.get(j);
				if (fl.getSender() == task) {
					System.out
							.println(fl.getName() + " "
									+ fl.getReceiver().getName() + " "
									+ fl.getVolume());
				}

			}

		}

	}

	protected SimpleTask getTask(String id) {

		Iterator i = taskGraph.getVertices().iterator();
		while (i.hasNext()) {
			SimpleTask task = (SimpleTask) i.next();
			if (task.getName().equals(id))
				return task;
		}

		return null;

	}

	protected SimpleCommunication getCommunication(String id) {

		Iterator i = flows.iterator();
		while (i.hasNext()) {
			SimpleCommunication flow = (SimpleCommunication) i.next();
			if (flow.getName() == id)
				return flow;
		}

		return null;

	}

	public static void main(String[] args) {

		MCSLBenchmark tb = new MCSLBenchmark(1);

	}

	public void generateModel() {

		int pri = 0;
		for (int i = 0; i < taskGraph.getVertices().size(); i++) {

			SimpleTask task = getTask("t" + i);
			boolean comp = true;

			for (int j = 0; j < flows.size(); j++) {
				SimpleCommunication fl = flows.get(j);
				if (fl.getSender() == task) {

					String part1 = "    <entity name=\"";

					String part2 = fl.getName() + "-"
							+ fl.getSender().getName() + "-"
							+ fl.getReceiver().getName(); // insert actor name

					String part3 = "\" class=\"ptolemy.vergil.uml.UMLSeqActor\">"
							+ "        <property name=\"_tableauFactory\" class=\"ptolemy.vergil.uml.UMLTableauFactory\">"
							+ "        </property>"
							+ "        <property name=\"Lifeline\" class=\"ptolemy.vergil.uml.Lifeline\" value=\"\">"
							+ "            <property name=\"nameUML\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"";

					String part4 = fl.getSender().getName(); // insert sender
																// task name

					String part5 = "\">"
							+ "            </property>"
							+ "            <property name=\"location\" class=\"ptolemy.kernel.util.Location\" value=\"{185.0, 30.0}\">"
							+ "            </property>"
							+ "            <property name=\"LifelineIcon\" class=\"ptolemy.vergil.uml.LifelineIcon\">"
							+ "                <property name=\"image\" class=\"ptolemy.vergil.kernel.attributes.ImageAttribute\">"
							+ "                    <property name=\"_hideName\" class=\"ptolemy.data.expr.SingletonParameter\" value=\"true\">"
							+ "                    </property>"
							+ "                    <property name=\"_hideAllParameters\" class=\"ptolemy.data.expr.Parameter\" value=\"true\">"
							+ "                    </property>"
							+ "                    <property name=\"source\" class=\"ptolemy.data.expr.FileParameter\" value=\"$PTII/ptolemy/vergil/uml/Lifeline.gif\">"
							+ "                    </property>"
							+ "                    <property name=\"scale\" class=\"ptolemy.data.expr.Parameter\" value=\"100.0\">"
							+ "                    </property>"
							+ "                </property>"
							+ "            </property>"
							+ "        </property>"
							+ "        <property name=\"Lifeline2\" class=\"ptolemy.vergil.uml.Lifeline\" value=\"\">"
							+ "            <property name=\"nameUML\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"";

					String part6 = fl.getReceiver().getName(); // insert
																// destination
																// task name

					String part7 = "\">"
							+ "            </property>"
							+ "            <property name=\"location\" class=\"ptolemy.kernel.util.Location\" value=\"{390.0, 30.0}\">"
							+ "            </property>"
							+ "            <property name=\"LifelineIcon\" class=\"ptolemy.vergil.uml.LifelineIcon\">"
							+ "                <property name=\"image\" class=\"ptolemy.vergil.kernel.attributes.ImageAttribute\">"
							+ "                    <property name=\"_hideName\" class=\"ptolemy.data.expr.SingletonParameter\" value=\"true\">"
							+ "                    </property>"
							+ "                    <property name=\"_hideAllParameters\" class=\"ptolemy.data.expr.Parameter\" value=\"true\">"
							+ "                    </property>"
							+ "                    <property name=\"source\" class=\"ptolemy.data.expr.FileParameter\" value=\"$PTII/ptolemy/vergil/uml/Lifeline.gif\">"
							+ "                    </property>"
							+ "                    <property name=\"scale\" class=\"ptolemy.data.expr.Parameter\" value=\"100.0\">"
							+ "                    </property>"
							+ "                </property>"
							+ "            </property>"
							+ "        </property>"
							+ "        <property name=\"m\" class=\"ptolemy.vergil.uml.Message\" value=\"\">"
							+ "            <property name=\"nameUML\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"m\">"
							+ "            </property>"
							+ "            <property name=\"msgSort\" class=\"ptolemy.data.expr.Parameter\" value=\"0\">"
							+ "            </property>"
							+ "            <property name=\"receiveEvent\" class=\"ptolemy.vergil.uml.MessageOccurrenceSpecification\" value=\"ReceiveEvent\">"
							+ "                <property name=\"location\" class=\"ptolemy.kernel.util.Location\" value=\"{0.0, 117.0}\">"
							+ "                </property>"
							+ "                <property name=\"Lifeline2\" class=\"ptolemy.vergil.uml.Lifeline\" value=\"\">"
							+ "                    <property name=\"location\" class=\"ptolemy.kernel.util.Location\" value=\"{390.0, 30.0}\">"
							+ "                    </property>"
							+ "                </property>"
							+ "            </property>"
							+ "            <property name=\"sendEvent\" class=\"ptolemy.vergil.uml.MessageOccurrenceSpecification\" value=\"SendEvent\">"
							+ "                <property name=\"location\" class=\"ptolemy.kernel.util.Location\" value=\"{0.0, 117.0}\">"
							+ "                </property>"
							+ "                <property name=\"Lifeline\" class=\"ptolemy.vergil.uml.Lifeline\" value=\"\">"
							+ "                    <property name=\"location\" class=\"ptolemy.kernel.util.Location\" value=\"{185.0, 30.0}\">"
							+ "                    </property>"
							+ "                </property>"
							+ "            </property>"
							+ "            <property name=\"preCompTime\" class=\"ptolemy.data.expr.Parameter\" value=\"";

					String part8 = "0.0";
					if (comp) {
						part8 = "" + task.getCost();
						comp = false;
					} // insert preCompTime

					String part9 = "\">"
							+ "            </property>"
							+ "            <property name=\"totalPacketSize\" class=\"ptolemy.data.expr.Parameter\" value=\"";

					String part10 = "" + fl.getVolume();// insert # payload
														// flits

					String part11 = "\">"
							+ "            </property>"
							+ "            <property name=\"subPacketSize\" class=\"ptolemy.data.expr.Parameter\" value=\"500000\">"
							+ "            </property>"
							+ "            <property name=\"priority\" class=\"ptolemy.data.expr.Parameter\" value=\"";

					String part12 = "" + pri++; // insert priority

					String part13 = "\">"
							+ "            </property>"
							+ "            <property name=\"delay\" class=\"ptolemy.data.expr.Parameter\" value=\"\">"
							+ "            </property>"
							+ "            <property name=\"size\" class=\"ptolemy.data.expr.Parameter\" value=\"\">"
							+ "            </property>"
							+ "        </property>"
							+ "        <property name=\"Partial Order\" class=\"lsi.noc.application.uml.directors.PartialOrder\">"
							+ "            <property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{60, 75}\">"
							+ "            </property>"
							+ "        </property>"
							+ "        <property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{0.0, ";

					String part14 = pri * 100 + ".0"; // insert location

					String part15 = "}\">"
							+ "        </property>"
							+ "        <property name=\"_icon\" class=\"ptolemy.vergil.icon.EditorIcon\">"
							+ "        </property>"
							+ "        <port name=\"m_send_m\" class=\"ptolemy.vergil.uml.BehavioralPatternPort\">"
							+ "            <property name=\"input\"/>"
							+ "            <property name=\"m\" class=\"ptolemy.vergil.uml.Message\" value=\"\">"
							+ "            </property>"
							+ "            <property name=\"_showName\" class=\"ptolemy.data.expr.SingletonParameter\" value=\"true\">"
							+ "            </property>"
							+ "            <property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"-10.0,-10.0\">"
							+ "            </property>"
							+ "        </port>"
							+ "        <port name=\"m_receive_m\" class=\"ptolemy.vergil.uml.BehavioralPatternPort\">"
							+ "            <property name=\"output\"/>"
							+ "            <property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"-10.0,-10.0\">"
							+ "            </property>"
							+ "            <property name=\"m\" class=\"ptolemy.vergil.uml.Message\" value=\"\">"
							+ "            </property>"
							+ "            <property name=\"_showName\" class=\"ptolemy.data.expr.SingletonParameter\" value=\"true\">"
							+ "            </property>"
							+ "        </port>"
							+ "        <relation name=\"m\" class=\"ptolemy.actor.TypedIORelation\">"
							+ "            <property name=\"width\" class=\"ptolemy.data.expr.Parameter\" value=\"1\">"
							+ "            </property>"
							+ "        </relation>"
							+ "        <link port=\"m_send_m\" relation=\"m\"/>"
							+ "        <link port=\"m_receive_m\" relation=\"m\"/>"
							+ "    </entity>";

					System.out.println(part1 + part2 + part3 + part4 + part5
							+ part6 + part7 + part8 + part9 + part10 + part11
							+ part12 + part13 + part14 + part15);
				}
			}

		}

	}

}
