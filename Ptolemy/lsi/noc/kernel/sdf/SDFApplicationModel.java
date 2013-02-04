package lsi.noc.kernel.sdf;

import lsi.noc.kernel.ApplicationModel;

public class SDFApplicationModel extends ApplicationModel {

	public SDFApplicationModel() {
		super();

	}

	public static void main(String[] args) {

		SDFApplicationModel graph = new SDFApplicationModel();

		SDFTask a = new SDFTask();
		a.setName("a");
		SDFTask b = new SDFTask();
		b.setName("b");
		SDFTask c = new SDFTask();
		c.setName("c");
		SDFTask d = new SDFTask();
		d.setName("d");
		SDFTask e = new SDFTask();
		e.setName("e");
		SDFTask f = new SDFTask();
		f.setName("f");
		SDFTask g = new SDFTask();
		g.setName("g");
		SDFTask h = new SDFTask();
		h.setName("h");
		SDFTask i = new SDFTask();
		i.setName("i");
		SDFTask j = new SDFTask();
		j.setName("j");
		SDFTask k = new SDFTask();
		k.setName("k");
		SDFTask l = new SDFTask();
		l.setName("l");

		SDFCommunication c1 = new SDFCommunication(a, c);
		SDFCommunication c2 = new SDFCommunication(b, c);
		SDFCommunication c3 = new SDFCommunication(a, f);
		SDFCommunication c4 = new SDFCommunication(c, d);
		SDFCommunication c5 = new SDFCommunication(d, b);
		SDFCommunication c6 = new SDFCommunication(f, a);
		SDFCommunication c7 = new SDFCommunication(f, g);
		SDFCommunication c8 = new SDFCommunication(d, e);
		SDFCommunication c9 = new SDFCommunication(d, h);
		SDFCommunication c10 = new SDFCommunication(h, i);
		SDFCommunication c11 = new SDFCommunication(i, k);
		SDFCommunication c12 = new SDFCommunication(j, k);
		SDFCommunication c13 = new SDFCommunication(k, l);
		SDFCommunication c14 = new SDFCommunication(e, g);
		SDFCommunication c15 = new SDFCommunication(g, l);

		SDFCommunication[] ain = { c6 };
		int[] acons = { 2 };
		SDFCommunication[] aout = { c1, c3 };
		int[] aprod = { 8, 1 };
		a.setChannels(ain, aout, acons, aprod);

		SDFCommunication[] bin = { c5 };
		int[] bcons = { 1 };
		SDFCommunication[] bout = { c2 };
		int[] bprod = { 4 };
		b.setChannels(bin, bout, bcons, bprod);

		SDFCommunication[] cin = { c1, c2 };
		int[] ccons = { 1, 1 };
		SDFCommunication[] cout = { c4 };
		int[] cprod = { 2 };
		c.setChannels(cin, cout, ccons, cprod);

		SDFCommunication[] din = { c4 };
		int[] dcons = { 8 };
		SDFCommunication[] dout = { c5, c8, c9 };
		int[] dprod = { 1, 3, 3 };
		d.setChannels(din, dout, dcons, dprod);

		SDFCommunication[] ein = { c8 };
		int[] econs = { 3 };
		SDFCommunication[] eout = { c14 };
		int[] eprod = { 1 };
		e.setChannels(ein, eout, econs, eprod);

		SDFCommunication[] fin = { c3 };
		int[] fcons = { 8 };
		SDFCommunication[] fout = { c6, c7 };
		int[] fprod = { 16, 16 };
		f.setChannels(fin, fout, fcons, fprod);

		SDFCommunication[] gin = { c7, c14 };
		int[] gcons = { 2, 2 };
		SDFCommunication[] gout = { c15 };
		int[] gprod = { 1 };
		g.setChannels(gin, gout, gcons, gprod);

		SDFCommunication[] hin = { c9 };
		int[] hcons = { 2 };
		SDFCommunication[] hout = { c10 };
		int[] hprod = { 2 };
		h.setChannels(hin, hout, hcons, hprod);

		SDFCommunication[] iin = { c10 };
		int[] icons = { 4 };
		SDFCommunication[] iout = { c11 };
		int[] iprod = { 1 };
		i.setChannels(iin, iout, icons, iprod);

		SDFCommunication[] jin = {};
		int[] jcons = {};
		SDFCommunication[] jout = { c12 };
		int[] jprod = { 1 };
		j.setChannels(jin, jout, jcons, jprod);

		SDFCommunication[] kin = { c11, c12 };
		int[] kcons = { 1, 4 };
		SDFCommunication[] kout = { c13 };
		int[] kprod = { 2 };
		k.setChannels(kin, kout, kcons, kprod);

		SDFCommunication[] lin = { c13, c15 };
		int[] lcons = { 3, 1 };
		SDFCommunication[] lout = {};
		int[] lprod = {};
		l.setChannels(lin, lout, lcons, lprod);

		try {

			/*
			 * 
			 * c5.writeTokens(2); c6.writeTokens(16);
			 * 
			 * for(int loop=0;loop<8;loop++){
			 * 
			 * a.fire(); b.fire(2); c.fire(8); d.fire(2);
			 * 
			 * }
			 * 
			 * f.fire(); j.fire(48); h.fire(24); i.fire(12); k.fire(12);
			 * e.fire(16); g.fire(8); l.fire(8);
			 */

			c5.writeTokens(16);
			c6.writeTokens(16);

			for (int loop = 0; loop < 8; loop++) {

				a.fire();
				b.fire(2);
				c.fire(8);
			}

			f.fire();
			d.fire(16);

			for (int loop = 0; loop < 6; loop++) {
				h.fire(2);
				i.fire();
			}

			for (int loop = 0; loop < 8; loop++) {
				e.fire(2);
				g.fire();
			}

			for (int loop = 0; loop < 6; loop++) {
				h.fire(2);
				i.fire();
			}

			j.fire(48);
			k.fire(12);
			l.fire(8);

		} catch (Exception ex) {
			System.out.println(ex);
		}

		System.out.println("max: ");

		System.out.println("c1: " + c1.getMaxTokens());
		System.out.println("c2: " + c2.getMaxTokens());
		System.out.println("c3: " + c3.getMaxTokens());
		System.out.println("c4: " + c4.getMaxTokens());
		System.out.println("c5: " + c5.getMaxTokens());
		System.out.println("c6: " + c6.getMaxTokens());
		System.out.println("c7: " + c7.getMaxTokens());
		System.out.println("c8: " + c8.getMaxTokens());
		System.out.println("c9: " + c9.getMaxTokens());
		System.out.println("c10: " + c10.getMaxTokens());
		System.out.println("c11: " + c11.getMaxTokens());
		System.out.println("c12: " + c12.getMaxTokens());
		System.out.println("c13: " + c13.getMaxTokens());
		System.out.println("c14: " + c14.getMaxTokens());
		System.out.println("c15: " + c15.getMaxTokens());

		System.out.println("current: ");

		System.out.println("c1: " + c1.getCurrentTokens());
		System.out.println("c2: " + c2.getCurrentTokens());
		System.out.println("c3: " + c3.getCurrentTokens());
		System.out.println("c4: " + c4.getCurrentTokens());
		System.out.println("c5: " + c5.getCurrentTokens());
		System.out.println("c6: " + c6.getCurrentTokens());
		System.out.println("c7: " + c7.getCurrentTokens());
		System.out.println("c8: " + c8.getCurrentTokens());
		System.out.println("c9: " + c9.getCurrentTokens());
		System.out.println("c10: " + c10.getCurrentTokens());
		System.out.println("c11: " + c11.getCurrentTokens());
		System.out.println("c12: " + c12.getCurrentTokens());
		System.out.println("c13: " + c13.getCurrentTokens());
		System.out.println("c14: " + c14.getCurrentTokens());
		System.out.println("c15: " + c15.getCurrentTokens());

	}

}
