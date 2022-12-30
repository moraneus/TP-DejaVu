package dejavu

import java.nio.file.Paths

object Settings {
  var PROJECT_DIR = s"${Paths.get(".").toAbsolutePath}"
}
