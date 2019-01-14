package test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.redhat.et.libguestfs.GuestFS;
import com.redhat.et.libguestfs.LibGuestFSException;
import com.redhat.et.libguestfs.Stat;
import com.redhat.et.libguestfs.StatNS;
import com.redhat.et.libguestfs.StatVFS;
import com.redhat.et.libguestfs.TSKDirent;

public class Main {

	public static void main(String[] args) throws LibGuestFSException {
		System.out.println("test libguestfs-java");
		GuestFS g = new GuestFS();
		System.out.println("arik1");
		g.add_drive("/home/ahadas/fosdem/0fab5252-4ae7-4910-aa06-9801de74c0c9.qcow");
		System.out.println("arik2");
		g.launch();
		System.out.println("arik3");
		String roots[] = g.inspect_os ();
		System.out.println("arik4");
		for (String root : roots) {
			g.filesystem_walk(root);
			Map<String, String> fs = g.inspect_get_mountpoints(root);
			System.out.println("Root device: " + root);
			fs.forEach((k, v) -> {
				try {
					System.out.println("mounting " + v + " " + k);
					g.mount(v, k);
				} catch (LibGuestFSException e) {
					System.out.println("skipping: " + v);
				}
			});
			new Main().visit(g, "/");

/*			System.out.println("root: " + root);
			Map<String,String> mps = g.inspect_get_mountpoints (root);
            List<String> mps_keys = new ArrayList<String> (mps.keySet ());

            for (String mp : mps_keys) {
                String dev = mps.get (mp);
                try {
                    g.mount_ro (dev, mp);
                } catch (Exception exn) {
                    System.err.println (exn + " (ignored)");
                }
            }
            String base = "/bin";
			for (String file : g.ls("/bin")) {
				String path = base + "/" + file;
				if (g.is_file(path)) {
					System.out.println("  file: " + file);
				} else if (g.is_symlink(path)) {
					System.out.println("  link: " + file);
				} else {
					System.out.println("  directory: " + file);
				}
			} */
		}
	}

	public int visit(GuestFS g, String dir) throws LibGuestFSException {
		return visit(g, 0, dir);
	}

	private int visit(GuestFS g, int depth, String dir) throws LibGuestFSException {
		if (depth == 0) {
			StatNS s = g.statns(dir);
			if (visitorFunction(dir, null, s) < 0) {
				return -1;
			}
		}
		String[] names = g.ls(dir);
		StatNS[] stats = g.lstatnslist(dir, names);
		for (int i=0; i<names.length; ++i) {
			if (visitorFunction(dir, names[i], stats[i]) < 0)
				return -1;
			if (isDir(stats[i].st_mode)) {
				dir = dir.endsWith("/") ? dir.substring(0, dir.length()-1) : dir;
				if (visit(g, depth+1, dir+"/"+names[i])<0) {
					return -1;
				}
			}
		}
		return 0;
	}

	private boolean isDir(long mode) {
		return (mode & 0170000) == 0040000;
	}
	private int visitorFunction(String dir, String name, StatNS stats) {
		System.out.println(dir+"/"+name+" ; "+stats.st_size+" ; "+stats.st_mtime_sec);
		return 0;
	}
}