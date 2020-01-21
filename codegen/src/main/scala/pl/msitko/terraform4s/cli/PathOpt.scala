package pl.msitko.terraform4s.cli

import java.nio.file.Path

import com.monovore.decline.Opts

object PathOpt {

  def apply(long: String, help: String): Opts[os.Path] =
    Opts.option[Path](long = long, help = help).map { p =>
      if (p.isAbsolute) {
        os.Path(p)
      } else {
        os.pwd / os.RelPath(p.toString)
      }
    }
}
