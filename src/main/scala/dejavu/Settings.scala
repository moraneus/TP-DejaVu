package dejavu

import java.nio.file.Paths

object Settings {
  // It would be better if the following was computed automatically
  var PROJECT_DIR = s"${Paths.get(".").toAbsolutePath}"
}
