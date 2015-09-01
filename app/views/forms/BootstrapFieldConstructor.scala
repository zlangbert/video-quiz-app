package views.forms

object BootstrapFieldConstructor {

  import views.html.helper.FieldConstructor
  implicit val myFields = FieldConstructor(views.html.forms.fieldConstructor.f)
}
