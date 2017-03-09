local lfs = require ("lfs")
localPath         = {
getLocalPath = function ()
                  return lfs.currentdir() .. "/"
               end
}

local config = {
  batchSize         = 3,
  momentum          = 0,
  learningRate      = 1e-2,
  weightDecay       = 1e-5,
  learningRateDecay = 1e-7,
  epochnm           = 15,
  modelPath         = localPath.getLocalPath() .. 'models/',
  with_plotting     = true,
  data_file_path    = localPath.getLocalPath() .. 'data/save.dat',
  pathToImages      = localPath.getLocalPath() .. 'images/',
  pathToTestImages  = "/home/sbt-ulyanov-ka/tmp/shared/",
  categories        = {"button", "checkbox", "input", "other"},
  imagesSize        = {x = 200, y = 30},
  channels          = 1,
  trainPortion      = 0.7,
  numImages         = 10
}



return config
