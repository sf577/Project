package lsi.noc.kernel.testbench;

import java.util.StringTokenizer;

import lsi.noc.kernel.priority.PriorityApplicationModel;
import lsi.noc.kernel.priority.PriorityTask;
import lsi.noc.kernel.priority.PriorityTrafficFlow;

public class AutonomousVehicleApplication extends PriorityApplicationModel {

	public AutonomousVehicleApplication() {

		String[] flowdesc = generateDescriptor(2);

		for (int i = 0; i < flowdesc.length; i++) {

			StringTokenizer st = new StringTokenizer(flowdesc[i]);
			String so = st.nextToken();
			String de = st.nextToken();
			String scomptime = st.nextToken();
			double comptime = Double.parseDouble(scomptime);
			String bits = st.nextToken();
			String flits = st.nextToken();
			String total = st.nextToken();
			int payload = Integer.parseInt(total);
			String speriod = st.nextToken();
			double period = Double.parseDouble(speriod);
			String spriority = st.nextToken();
			int priority = Integer.parseInt(spriority);
			// String sjitter = st.nextToken();
			// double jitter = Double.parseDouble(sjitter);

			PriorityTask source = new PriorityTask(priority, comptime);
			source.setName(so);
			source.setPeriod(period);

			PriorityTask dest = new PriorityTask(priority * 100, 0.0);
			dest.setName(de);
			dest.setPeriod(period);

			source.setReleaseJitter(0);
			dest.setReleaseJitter(0);
			this.addTask(source);
			this.addTask(dest);

			PriorityTrafficFlow flow = new PriorityTrafficFlow(source, dest);
			flow.setPeriod(period);
			flow.setPriority(priority);
			flow.setPayload(payload);

			this.addFlow(flow);

		}

	}

	public String[] generateDescriptor(int version) {

		if (version == 2) {

			// creates traffic flows according to AV Application MADES v2.1

			String[] descriptor = new String[39];
			descriptor[0] = " POSI NAVC 0.005 16384 1024 1026 0.5 31 ";
			descriptor[1] = " NAVC OBDB 0.01 32768 2048 2050 0.5 32 ";
			descriptor[2] = " OBDB NAVC 0.15 262144 16384 16386 0.5 33 ";
			descriptor[3] = " OBDB OBMG 0.15 524288 32768 32770 1 34 ";
			descriptor[4] = " NAVC DIRC 0.02 8192 512 514 0.1 24 ";
			descriptor[5] = " SPES NAVC 0.005 8192 512 514 0.1 25 ";
			descriptor[6] = " NAVC THRC 0.01 16384 1024 1026 0.1 26 ";
			descriptor[7] = " FBU3 VOD1 0.01 614400 38400 38402 0.04 1 ";
			descriptor[8] = " FBU8 VOD2 0.01 614400 38400 38402 0.04 2 ";
			descriptor[9] = " VOD1 NAVC 0.02 8192 512 514 0.5 3 ";
			descriptor[10] = " VOD2 NAVC 0.02 8192 512 514 0.5 4 ";
			descriptor[11] = " FBU1 BFE1 0.01 614400 38400 38402 0.04 5 ";
			descriptor[12] = " FBU2 BFE2 0.01 614400 38400 38402 0.04 6 ";
			descriptor[13] = " FBU3 BFE3 0.01 614400 38400 38402 0.04 7 ";
			descriptor[14] = " FBU4 BFE4 0.01 614400 38400 38402 0.04 8 ";
			descriptor[15] = " FBU5 BFE5 0.01 614400 38400 38402 0.04 9 ";
			descriptor[16] = " FBU6 BFE6 0.01 614400 38400 38402 0.04 10 ";
			descriptor[17] = " FBU7 BFE7 0.01 614400 38400 38402 0.04 11 ";
			descriptor[18] = " FBU8 BFE8 0.01 614400 38400 38402 0.04 12 ";
			descriptor[19] = " BFE1 FDF1 0.02 32768 2048 2050 0.04 13 ";
			descriptor[20] = " BFE2 FDF1 0.02 32768 2048 2050 0.04 14 ";
			descriptor[21] = " BFE3 FDF1 0.02 32768 2048 2050 0.04 15 ";
			descriptor[22] = " BFE4 FDF1 0.02 32768 2048 2050 0.04 16 ";
			descriptor[23] = " BFE5 FDF2 0.02 32768 2048 2050 0.04 17 ";
			descriptor[24] = " BFE6 FDF2 0.02 32768 2048 2050 0.04 18 ";
			descriptor[25] = " BFE7 FDF2 0.02 32768 2048 2050 0.04 19 ";
			descriptor[26] = " BFE8 FDF2 0.02 32768 2048 2050 0.04 20 ";
			descriptor[27] = " FDF1 STPH 0.01 131072 8192 8194 0.04 21 ";
			descriptor[28] = " FDF2 STPH 0.01 131072 8192 8194 0.04 22 ";
			descriptor[29] = " STPH OBMG 0.03 65536 4096 4098 0.04 23 ";
			descriptor[30] = " POSI OBMG 0.005 16384 1024 1026 0.5 35 ";
			descriptor[31] = " USOS OBMG 0.005 16384 1024 1026 0.1 27 ";
			descriptor[32] = " OBMG OBDB 0.02 65536 4096 4098 1 37 ";
			descriptor[33] = " TPMS STAC 0.005 32768 2048 2050 0.5 36 ";
			descriptor[34] = " VIBS STAC 0.005 8192 512 514 0.1 28 ";
			descriptor[35] = " STAC TPMS 0.01 32768 2048 2050 1 38 ";
			descriptor[36] = " SPES STAC 0.005 16384 1024 1026 0.1 29 ";
			descriptor[37] = " STAC THRC 0.01 16384 1024 1026 0.1 30 ";
			descriptor[38] = " OBMG OBDB 0.0005 32768 2048 2050 1 39 ";
			return descriptor;

		}

		else {

			// creates traffic flows according to the strings below
			// models BasicVehicleApplication v0.6 with 320/240 resolution, 8
			// bit color and 128 bit flits

			String[] flowdesc = new String[39];

			flowdesc[1] = "BFE1 FDF1 0.02 7680 60 62 0.04 13";
			flowdesc[2] = "BFE2 FDF1 0.02 7680 60 62 0.04 14";
			flowdesc[3] = "BFE3 FDF1 0.02 7680 60 62 0.04 15";
			flowdesc[4] = "BFE4 FDF1 0.02 7680 60 62 0.04 16";
			flowdesc[5] = "BFE5 FDF2 0.02 7680 60 62 0.04 17";
			flowdesc[6] = "BFE6 FDF2 0.02 7680 60 62 0.04 18";
			flowdesc[7] = "BFE7 FDF2 0.02 7680 60 62 0.04 19";
			flowdesc[8] = "BFE8 FDF2 0.02 7680 60 62 0.04 20";
			flowdesc[9] = "FBU1 BFE1 0.01 153600 1200 1202 0.04 5";
			flowdesc[10] = "FBU2 BFE2 0.01 153600 1200 1202 0.04 6";
			flowdesc[11] = "FBU3 BFE3 0.01 153600 1200 1202 0.04 1";
			flowdesc[12] = "FBU3 VOD1 0.01 153600 1200 1202 0.04 7";
			flowdesc[13] = "FBU4 BFE4 0.01 153600 1200 1202 0.04 8";
			flowdesc[14] = "FBU5 BFE5 0.01 153600 1200 1202 0.04 9";
			flowdesc[15] = "FBU6 BFE6 0.01 153600 1200 1202 0.04 10";
			flowdesc[16] = "FBU7 BFE7 0.01 153600 1200 1202 0.04 11";
			flowdesc[17] = "FBU8 BFE8 0.01 153600 1200 1202 0.04 2";
			flowdesc[18] = "FBU8 VOD2 0.01 153600 1200 1202 0.04 12";
			flowdesc[19] = "FDF1 STPH 0.01 30720 240 242 0.04 21";
			flowdesc[20] = "FDF2 STPH 0.01 30720 240 242 0.04 22";
			flowdesc[21] = "NAVC DIRC 0.02 16384 128 130 0.1 24";
			flowdesc[22] = "NAVC OBDB 0.01 32768 256 258 2.0 37";
			flowdesc[23] = "NAVC THRC 0.01 32768 256 258 0.1 26";
			flowdesc[24] = "OBDB NAVC 0.15 524288 4096 4098 2.0 38";
			flowdesc[25] = "OBDB OBMG 0.15 524288 4096 4098 2.0 36";
			flowdesc[26] = "OBMG OBDB 0.4 131072 1024 1026 1 34";
			flowdesc[27] = "POSI NAVC 0.005 16384 128 130 0.5 31";
			flowdesc[28] = "POSI OBMG 0.005 16384 128 130 0.5 32";
			flowdesc[29] = "SPES NAVC 0.005 16384 128 130 0.1 25";
			flowdesc[30] = "SPES STAC 0.005 32768 256 258 0.1 29";
			flowdesc[31] = "STAC THRC 0.01 16384 128 130 0.1 30";
			flowdesc[32] = "STAC TPRC 0.02 32768 256 258 1 35";
			flowdesc[33] = "STPH OBMG 0.02 131072 1024 1026 0.04 23";
			flowdesc[34] = "TPMS STAC 0.005 65536 512 514 0.5 33";
			flowdesc[35] = "USOS OBMG 0.005 32768 256 258 0.1 27";
			flowdesc[36] = "VIBS STAC 0.005 16384 128 130 0.1 28";
			flowdesc[37] = "VOD1 NAVC 0.02 16384 128 130 0.04 3";
			flowdesc[0] = "VOD2 NAVC 0.02 16384 128 130 0.04 4";

			return flowdesc;

		}

	}

}
